package ge.kursi.settlement_funding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import ge.kursi.settlement_funding.algorithm.FundingAlgorithm;
import ge.kursi.settlement_funding.dto.FundingRequestAuditResponse;
import ge.kursi.settlement_funding.dto.FundingRequestResponse;
import ge.kursi.settlement_funding.dto.SettlementInstructionRequest;
import ge.kursi.settlement_funding.dto.SettlementRequest;
import ge.kursi.settlement_funding.model.FundingInstruction;
import ge.kursi.settlement_funding.model.FundingRequest;
import ge.kursi.settlement_funding.repository.FundingRequestRepository;

@ExtendWith(MockitoExtension.class)
class FundingRequestServiceTest {

    @Mock
    private FundingRequestRepository fundingRequestRepository;

    @Mock
    private FundingAlgorithm fundingAlgorithm;

    private FundingRequestService service;

    @BeforeEach
    void setUp() {
        service = new FundingRequestService(fundingRequestRepository, fundingAlgorithm);
    }

    @Test
    void fundSelectsInstructionsAndCalculatesTotals() {
        SettlementRequest request = new SettlementRequest(
                20_000L,
                List.of(
                        new SettlementInstructionRequest("INS-1", 7_000L, 150L),
                        new SettlementInstructionRequest("INS-2", 9_000L, 210L),
                        new SettlementInstructionRequest("INS-3", 4_000L, 90L)
                )
        );

        when(fundingAlgorithm.solve(any(long[].class), any(long[].class), anyLong()))
                .thenReturn(List.of(0, 2));
        when(fundingRequestRepository.save(any(FundingRequest.class)))
                .thenAnswer(invocation -> {
                    FundingRequest saved = invocation.getArgument(0);
                    saved.setRequestId(UUID.randomUUID());
                    return saved;
                });

        FundingRequestResponse response = service.fund(request);

        assertEquals(2, response.selectedInstructions().size());
        assertEquals("INS-1", response.selectedInstructions().get(0).instructionReference());
        assertEquals("INS-3", response.selectedInstructions().get(1).instructionReference());
        assertEquals(11_000L, response.totalSettlementConsumed());
        assertEquals(240L, response.totalExpectedFee());
        verify(fundingRequestRepository).save(any(FundingRequest.class));
    }

    @Test
    void fundReturnsEmptySelectionAndZeroTotalsWhenNothingFits() {
        SettlementRequest request = new SettlementRequest(
                3_000L,
                List.of(new SettlementInstructionRequest("INS-1", 7_000L, 150L))
        );

        when(fundingAlgorithm.solve(any(long[].class), any(long[].class), anyLong()))
                .thenReturn(List.of());
        when(fundingRequestRepository.save(any(FundingRequest.class)))
                .thenAnswer(invocation -> {
                    FundingRequest saved = invocation.getArgument(0);
                    saved.setRequestId(UUID.randomUUID());
                    return saved;
                });

        FundingRequestResponse response = service.fund(request);

        assertEquals(List.of(), response.selectedInstructions());
        assertEquals(0L, response.totalSettlementConsumed());
        assertEquals(0L, response.totalExpectedFee());
    }

    @Test
    void findAllSplitsSelectedAndDiscardedInstructions() {
        UUID requestId = UUID.randomUUID();
        FundingRequest request = new FundingRequest(
                20_000L,
                OffsetDateTime.parse("2026-09-07T09:00:00Z")
        );
        request.setRequestId(requestId);
        request.setTotalSettlementConsumed(7_000L);
        request.setTotalExpectedFee(150L);

        FundingInstruction selected = new FundingInstruction("INS-1", 7_000L, 150L, 0);
        selected.setSelected(true);
        request.addInstruction(selected);
        request.addInstruction(new FundingInstruction("INS-2", 9_000L, 210L, 1));

        when(fundingRequestRepository.findAllByOrderByCreatedAtDesc(any()))
                .thenReturn(new PageImpl<>(List.of(request), PageRequest.of(0, 20), 1));

        FundingRequestAuditResponse response = service
                .findAll(PageRequest.of(0, 20))
                .getContent()
                .get(0);

        assertEquals(List.of("INS-1"), response.selectedInstructions().stream()
                .map(instruction -> instruction.instructionReference())
                .toList());
        assertEquals(List.of("INS-2"), response.discardedInstructions().stream()
                .map(instruction -> instruction.instructionReference())
                .toList());
    }

    @Test
    void findByIdReturnsOnlySelectedInstructions() {
        UUID requestId = UUID.randomUUID();
        FundingRequest request = new FundingRequest(
                20_000L,
                OffsetDateTime.parse("2026-09-07T09:00:00Z")
        );
        request.setRequestId(requestId);
        request.setTotalSettlementConsumed(7_000L);
        request.setTotalExpectedFee(150L);

        FundingInstruction selected = new FundingInstruction("INS-1", 7_000L, 150L, 0);
        selected.setSelected(true);
        request.addInstruction(selected);
        request.addInstruction(new FundingInstruction("INS-2", 9_000L, 210L, 1));

        when(fundingRequestRepository.findById(requestId)).thenReturn(java.util.Optional.of(request));

        FundingRequestResponse response = service.findById(requestId);

        assertEquals(requestId, response.requestId());
        assertEquals(List.of("INS-1"), response.selectedInstructions().stream()
                .map(instruction -> instruction.instructionReference())
                .toList());
        assertEquals(7_000L, response.totalSettlementConsumed());
        assertEquals(150L, response.totalExpectedFee());
    }

    @Test
    void findByIdThrowsWhenRequestDoesNotExist() {
        UUID requestId = UUID.randomUUID();
        when(fundingRequestRepository.findById(requestId)).thenReturn(java.util.Optional.empty());

        assertThrows(FundingRequestNotFoundException.class,
                () -> service.findById(requestId));
    }
}
