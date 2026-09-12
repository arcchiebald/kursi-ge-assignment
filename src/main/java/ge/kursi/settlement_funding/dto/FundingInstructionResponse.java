package ge.kursi.settlement_funding.dto;

public record FundingInstructionResponse(
    String instructionReference,
    Long instructionAmount,
    Long expectedFee
) {

}
