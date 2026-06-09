package com.charter.rewards.service;

import com.charter.rewards.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Provides an in-memory dataset of sample customer transactions for demonstration purposes.
 *
 * <p>The dataset covers a three-month rolling window and includes five customers
 * with a variety of transaction amounts — including amounts below $50, between $50
 * and $100, and over $100 — to fully exercise all points-calculation tiers.
 *
 * <p>In a production system this service would be replaced with a repository backed
 * by a persistent data store (e.g., JPA / Spring Data).
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Service
public class TransactionDataService {

    /** The static in-memory transaction list. */
    private final List<Transaction> transactions;

    /**
     * Constructs the service and initialises the sample dataset.
     *
     * <p>The dataset spans January – March 2024 and includes:
     * <ul>
     *   <li>C001 – Alice Johnson: 6 transactions across all three months</li>
     *   <li>C002 – Bob Smith: 5 transactions across all three months</li>
     *   <li>C003 – Carol White: 5 transactions across all three months</li>
     *   <li>C004 – David Brown: 4 transactions with some very high-value purchases</li>
     *   <li>C005 – Eva Martinez: 4 transactions including amounts below the $50 threshold</li>
     * </ul>
     */
    public TransactionDataService() {
        transactions = List.of(
                // ── Alice Johnson (C001) ────────────────────────────────────
                buildTx("C001", "Alice Johnson", LocalDate.of(2024, 1, 5),  new BigDecimal("120.00")),
                buildTx("C001", "Alice Johnson", LocalDate.of(2024, 1, 18), new BigDecimal("75.50")),
                buildTx("C001", "Alice Johnson", LocalDate.of(2024, 2, 3),  new BigDecimal("200.00")),
                buildTx("C001", "Alice Johnson", LocalDate.of(2024, 2, 22), new BigDecimal("45.00")),
                buildTx("C001", "Alice Johnson", LocalDate.of(2024, 3, 10), new BigDecimal("130.75")),
                buildTx("C001", "Alice Johnson", LocalDate.of(2024, 3, 28), new BigDecimal("88.00")),

                // ── Bob Smith (C002) ─────────────────────────────────────────
                buildTx("C002", "Bob Smith",     LocalDate.of(2024, 1, 7),  new BigDecimal("55.00")),
                buildTx("C002", "Bob Smith",     LocalDate.of(2024, 1, 20), new BigDecimal("110.00")),
                buildTx("C002", "Bob Smith",     LocalDate.of(2024, 2, 14), new BigDecimal("95.00")),
                buildTx("C002", "Bob Smith",     LocalDate.of(2024, 3, 1),  new BigDecimal("150.00")),
                buildTx("C002", "Bob Smith",     LocalDate.of(2024, 3, 25), new BigDecimal("30.00")),

                // ── Carol White (C003) ───────────────────────────────────────
                buildTx("C003", "Carol White",   LocalDate.of(2024, 1, 11), new BigDecimal("250.00")),
                buildTx("C003", "Carol White",   LocalDate.of(2024, 1, 29), new BigDecimal("60.00")),
                buildTx("C003", "Carol White",   LocalDate.of(2024, 2, 8),  new BigDecimal("40.00")),
                buildTx("C003", "Carol White",   LocalDate.of(2024, 2, 19), new BigDecimal("175.00")),
                buildTx("C003", "Carol White",   LocalDate.of(2024, 3, 15), new BigDecimal("99.99")),

                // ── David Brown (C004) ───────────────────────────────────────
                buildTx("C004", "David Brown",   LocalDate.of(2024, 1, 3),  new BigDecimal("500.00")),
                buildTx("C004", "David Brown",   LocalDate.of(2024, 2, 10), new BigDecimal("350.00")),
                buildTx("C004", "David Brown",   LocalDate.of(2024, 2, 27), new BigDecimal("85.00")),
                buildTx("C004", "David Brown",   LocalDate.of(2024, 3, 20), new BigDecimal("420.00")),

                // ── Eva Martinez (C005) ──────────────────────────────────────
                buildTx("C005", "Eva Martinez",  LocalDate.of(2024, 1, 14), new BigDecimal("25.00")),
                buildTx("C005", "Eva Martinez",  LocalDate.of(2024, 1, 30), new BigDecimal("72.00")),
                buildTx("C005", "Eva Martinez",  LocalDate.of(2024, 2, 5),  new BigDecimal("105.00")),
                buildTx("C005", "Eva Martinez",  LocalDate.of(2024, 3, 12), new BigDecimal("49.99"))
        );
    }

    /**
     * Returns an unmodifiable view of all transactions in the dataset.
     *
     * @return list of all {@link Transaction} objects
     */
    public List<Transaction> getAllTransactions() {
        return transactions;
    }

    /**
     * Factory helper to construct a {@link Transaction} with a generated UUID.
     *
     * @param customerId   the customer's unique identifier
     * @param customerName the customer's display name
     * @param date         the transaction date
     * @param amount       the amount spent
     * @return a fully-populated {@link Transaction}
     */
    private Transaction buildTx(String customerId, String customerName,
                                  LocalDate date, BigDecimal amount) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .customerId(customerId)
                .customerName(customerName)
                .transactionDate(date)
                .amount(amount)
                .build();
    }
}
