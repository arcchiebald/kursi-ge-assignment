package ge.kursi.settlement_funding.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ge.kursi.settlement_funding.algorithm.FundingAlgorithm;
import ge.kursi.settlement_funding.dto.FundingInstructionResponse;
import ge.kursi.settlement_funding.dto.FundingRequestAuditResponse;
import ge.kursi.settlement_funding.dto.FundingRequestResponse;
import ge.kursi.settlement_funding.dto.SettlementInstructionRequest;
import ge.kursi.settlement_funding.dto.SettlementRequest;
import ge.kursi.settlement_funding.model.FundingInstruction;
import ge.kursi.settlement_funding.model.FundingRequest;
import ge.kursi.settlement_funding.repository.FundingRequestRepository;

@Service
public class FundingRequestService {

    private final FundingRequestRepository fundingRequestRepository;
    private final FundingAlgorithm fundingAlgorithm;

    public FundingRequestService(FundingRequestRepository fundingRequestRepository, FundingAlgorithm fundingAlgorithm) {
        this.fundingRequestRepository = fundingRequestRepository;
        this.fundingAlgorithm = fundingAlgorithm;
    }

    @Transactional 
    public FundingRequestResponse fund(SettlementRequest request) {
        Long availableBalance = request.availableSettlementBalance();
        List<SettlementInstructionRequest> candidates = request.candidateInstructions();

        FundingRequest fundingRequest = new FundingRequest(request.availableSettlementBalance(), OffsetDateTime.now());

        int count = candidates.size();

        long[] amounts = new long[count];
        long[] fees = new long[count];

        // Looping over every single candidateInstruction
        for (int i = 0; i < count; i++) {
            SettlementInstructionRequest candidate = candidates.get(i);

            amounts[i] = candidate.instructionAmount();
            fees[i] = candidate.expectedFee();

            // We map current local index(i) inside candidates List in database
            // in order to mark indices of selected candidates in 'is_selected' column
            FundingInstruction instruction = new FundingInstruction(
                candidate.instructionReference(), 
                candidate.instructionAmount(), 
                candidate.expectedFee(), 
                i
            );

            fundingRequest.addInstruction(instruction);
        }

        // ALGORITHM PART 
        List<Integer> selectedIndices = fundingAlgorithm.solve(amounts, fees, availableBalance);

        // Initialize variables for sum
        long totalSettlementConsumed = 0L;
        long totalExpectedFee = 0L;

        // Loop over every selected instruction index
        for (Integer selectedIndex : selectedIndices) {
            FundingInstruction instruction = fundingRequest.getInstructions().get(selectedIndex);

            // Mark that instruction as selected for audit purposes
            instruction.setSelected(true);

            // Add its values to sum variables
            totalSettlementConsumed += instruction.getInstructionAmount();
            totalExpectedFee += instruction.getExpectedFee();
        }

        // Set final values in FundingRequest
        fundingRequest.setTotalSettlementConsumed(totalSettlementConsumed);
        fundingRequest.setTotalExpectedFee(totalExpectedFee);

        FundingRequest savedFundingRequest = fundingRequestRepository.save(fundingRequest);

        // Convert all instructions to responses in order to use them in nested FundingRequestResponse
        List<FundingInstructionResponse> selectedInstructions = 
                        savedFundingRequest.getInstructions()
                        .stream()
                        .filter(FundingInstruction::isSelected)
                        .map(this::convertToResponse)
                        .toList();

        return new FundingRequestResponse(
            savedFundingRequest.getRequestId(),
            selectedInstructions,
            savedFundingRequest.getTotalSettlementConsumed(),
            savedFundingRequest.getTotalExpectedFee(),
            savedFundingRequest.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public FundingRequestResponse findById(UUID requestId) {
        FundingRequest fundingRequest = fundingRequestRepository.findById(requestId)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                            "Funding request not found: " + requestId
                                        ));

        List<FundingInstructionResponse> selectedInstructions = fundingRequest.getInstructions()
                                        .stream()
                                        .filter(FundingInstruction::isSelected)
                                        .map(this::convertToResponse)
                                        .toList();

        return new FundingRequestResponse(
                fundingRequest.getRequestId(),
                selectedInstructions,
                fundingRequest.getTotalSettlementConsumed(),
                fundingRequest.getTotalExpectedFee(),
                fundingRequest.getCreatedAt()
        );                        
    }

    @Transactional(readOnly = true)
    public Page<FundingRequestAuditResponse> findAll(Pageable pageable) {
        return fundingRequestRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::convertToAuditResponse);
    }



    // HELPER FUNCTIONS
    private FundingInstructionResponse convertToResponse(FundingInstruction instruction) {
        return new FundingInstructionResponse(
            instruction.getInstructionReference(),
            instruction.getInstructionAmount(),
            instruction.getExpectedFee()
        );
    }

    private FundingRequestAuditResponse convertToAuditResponse(FundingRequest fundingRequest) {
        List<FundingInstructionResponse> selectedInstructions = 
                                    fundingRequest.getInstructions()
                                    .stream()
                                    .filter(FundingInstruction::isSelected)
                                    .map(this::convertToResponse)
                                    .toList();

        List<FundingInstructionResponse> discardedInstructions = 
                                    fundingRequest.getInstructions()
                                    .stream()
                                    .filter(e -> !e.isSelected())
                                    .map(this::convertToResponse)
                                    .toList();    

        return new FundingRequestAuditResponse(
            fundingRequest.getRequestId(),
            selectedInstructions,
            discardedInstructions,
            fundingRequest.getTotalSettlementConsumed(),
            fundingRequest.getTotalExpectedFee(),
            fundingRequest.getCreatedAt()
        );
    }
}
