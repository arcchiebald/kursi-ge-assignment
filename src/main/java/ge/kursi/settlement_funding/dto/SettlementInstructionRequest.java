package ge.kursi.settlement_funding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SettlementInstructionRequest(
    @NotBlank String instructionReference,
    @NotNull @Positive Long instructionAmount,
    @NotNull @Positive Long expectedFee
) {

}
