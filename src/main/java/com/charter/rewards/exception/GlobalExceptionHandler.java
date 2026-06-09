package com.charter.rewards.exception;

import com.charter.rewards.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>Maps application-specific and framework exceptions to standardised
 * {@link ErrorResponseDto} responses with appropriate HTTP status codes.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link CustomerNotFoundException}, returning HTTP 404.
     *
     * @param ex the exception to handle
     * @return a 404 error response
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomerNotFound(CustomerNotFoundException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles {@link InvalidDateRangeException}, returning HTTP 400.
     *
     * @param ex the exception to handle
     * @return a 400 error response
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDateRange(InvalidDateRangeException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles Bean Validation failures on request parameters and bodies,
     * returning HTTP 400 with per-field error messages.
     *
     * @param ex the validation exception
     * @return a 400 error response containing all field-level errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields failed validation")
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles type mismatch errors in request parameters (e.g., invalid date format),
     * returning HTTP 400.
     *
     * @param ex the type mismatch exception
     * @return a 400 error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Parameter '%s' has an invalid value: '%s'. Expected type: %s",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Catch-all handler for any unexpected exceptions, returning HTTP 500.
     *
     * @param ex the exception to handle
     * @return a 500 error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
