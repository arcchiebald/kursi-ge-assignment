package ge.kursi.settlement_funding.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ge.kursi.settlement_funding.service.FundingRequestNotFoundException;

import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice 
public class ApiExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String,String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
        .getFieldErrors()
        .forEach(
            error -> errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {

        Map<String, String> error = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(FundingRequestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            FundingRequestNotFoundException exception) {

        return ResponseEntity.status(404)
                .body(Map.of("error", exception.getMessage()));
    }
}
