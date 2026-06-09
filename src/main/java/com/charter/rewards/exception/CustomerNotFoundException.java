package com.charter.rewards.exception;

/**
 * Exception thrown when a requested customer cannot be found in the data store.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code CustomerNotFoundException} with a message
     * specifying the customer identifier that was not found.
     *
     * @param customerId the customer ID that could not be located
     */
    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId);
    }
}
