package ge.kursi.settlement_funding.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;

public record SettlementRequest(
    @NotNull @PositiveOrZero Long availableSettlementBalance,
    @NotEmpty List<@Valid SettlementInstructionRequest> candidateInstructions
) {

}
