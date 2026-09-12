package ge.kursi.settlement_funding.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FundingRequestResponse(
    UUID requestId,
    List<FundingInstructionResponse> selectedInstructions,
    Long totalSettlementConsumed,
    Long totalExpectedFee,
    OffsetDateTime createdAt
) {

}
