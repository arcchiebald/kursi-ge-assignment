package ge.kursi.settlement_funding.dto;

import java.util.List;
import java.util.UUID;

import java.time.OffsetDateTime;

public record FundingRequestAuditResponse(
    UUID requestId,
    List<FundingInstructionResponse> selectedInstructions,
    List<FundingInstructionResponse> discardedInstructions,
    Long totalSettlementConsumed,
    Long totalExpectedFee,
    OffsetDateTime createdAt
) {

}
