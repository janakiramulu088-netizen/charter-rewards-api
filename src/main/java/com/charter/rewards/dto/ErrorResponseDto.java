package com.charter.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised error response returned by the API when a request fails.
 *
 * <p>Provides the HTTP status code, a human-readable message, a list of
 * field-level validation errors (where applicable), and the timestamp at
 * which the error occurred.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    /**
     * HTTP status code of the error (e.g., 400, 404, 500).
     */
    private int status;

    /**
     * Short description of the error type (e.g., "Bad Request").
     */
    private String error;

    /**
     * Detailed human-readable message explaining the error.
     */
    private String message;

    /**
     * List of field-level validation error messages, populated for 400 responses.
     * May be null or empty for non-validation errors.
     */
    private List<String> validationErrors;

    /**
     * Timestamp at which the error occurred.
     */
    private LocalDateTime timestamp;
}
