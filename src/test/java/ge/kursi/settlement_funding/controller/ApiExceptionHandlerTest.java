package ge.kursi.settlement_funding.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import ge.kursi.settlement_funding.service.FundingRequestNotFoundException;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void handleValidationMapsEachFieldErrorToItsMessage() {
        FieldError balanceError = new FieldError("settlementRequest", "availableSettlementBalance", "must not be null");
        FieldError referenceError = new FieldError("settlementRequest", "candidateInstructions[0].instructionReference", "must not be blank");

        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(balanceError, referenceError));

        ResponseEntity<?> response = handler.handleValidation(validationException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                java.util.Map.of(
                        "availableSettlementBalance", "must not be null",
                        "candidateInstructions[0].instructionReference", "must not be blank"
                ),
                response.getBody()
        );
    }

    @Test
    void handleIllegalArgumentReturnsBadRequestWithMessage() {
        ResponseEntity<?> response = handler.handleIllegalArgument(
                new IllegalArgumentException("capacity must be non-negative"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(java.util.Map.of("error", "capacity must be non-negative"), response.getBody());
    }

    @Test
    void handleNotFoundReturnsNotFoundWithMessage() {
        ResponseEntity<?> response = handler.handleNotFound(
                new FundingRequestNotFoundException("Funding request not found: 123"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(java.util.Map.of("error", "Funding request not found: 123"), response.getBody());
    }
}
