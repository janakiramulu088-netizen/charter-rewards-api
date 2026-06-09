package com.charter.rewards.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single purchase transaction made by a customer.
 *
 * <p>A transaction captures the customer identifier, the date of the purchase,
 * and the amount spent. This is the core domain object used to calculate
 * reward points.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * Unique identifier for this transaction.
     */
    private String transactionId;

    /**
     * Identifier of the customer who made the purchase.
     * Must not be blank.
     */
    @NotBlank(message = "Customer ID must not be blank")
    private String customerId;

    /**
     * Name of the customer for display purposes.
     */
    private String customerName;

    /**
     * Date on which the transaction occurred.
     * Must not be null.
     */
    @NotNull(message = "Transaction date must not be null")
    private LocalDate transactionDate;

    /**
     * Amount spent in the transaction in USD.
     * Must be a positive value greater than zero.
     */
    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
