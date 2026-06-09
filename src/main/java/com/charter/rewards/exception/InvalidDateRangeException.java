package com.charter.rewards.exception;

/**
 * Exception thrown when an invalid date range is supplied to a rewards query.
 *
 * <p>Common causes include a start date that is after the end date, or a range
 * that spans more than the maximum permitted number of months.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
public class InvalidDateRangeException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidDateRangeException} with the given message.
     *
     * @param message human-readable explanation of why the date range is invalid
     */
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
