package com.charter.rewards.service;

import com.charter.rewards.dto.CustomerRewardSummaryDto;
import com.charter.rewards.dto.MonthlyRewardDto;
import com.charter.rewards.dto.RewardsResponseDto;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.exception.InvalidDateRangeException;
import com.charter.rewards.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link RewardsService}.
 *
 * <p>Uses an in-memory dataset of transactions to compute reward points.
 * The points calculation follows the charter rules:
 * <ul>
 *   <li>2 points per dollar spent over $100 per transaction</li>
 *   <li>1 point per dollar spent between $50 and $100 per transaction</li>
 * </ul>
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Service
public class RewardsServiceImpl implements RewardsService {

    /** Threshold above which 2-point rate applies. */
    private static final double UPPER_THRESHOLD = 100.0;

    /** Threshold above which 1-point rate applies (up to the upper threshold). */
    private static final double LOWER_THRESHOLD = 50.0;

    private final TransactionDataService transactionDataService;

    /**
     * Constructs the service with the required transaction data provider.
     *
     * @param transactionDataService the service providing the transaction dataset
     */
    public RewardsServiceImpl(TransactionDataService transactionDataService) {
        this.transactionDataService = transactionDataService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates that the date range is not null and that the start date
     * does not fall after the end date before computing results.
     */
    @Override
    public RewardsResponseDto calculateRewardsForAllCustomers(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Transaction> transactions = transactionDataService.getAllTransactions().stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate)
                        && !t.getTransactionDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Group transactions by customerId
        Map<String, List<Transaction>> byCustomer = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<CustomerRewardSummaryDto> customerSummaries = byCustomer.entrySet().stream()
                .map(entry -> buildCustomerSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CustomerRewardSummaryDto::getCustomerId))
                .collect(Collectors.toList());

        String period = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                + " to "
                + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return RewardsResponseDto.builder()
                .period(period)
                .customers(customerSummaries)
                .totalTransactions(transactions.size())
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * @throws CustomerNotFoundException if no transactions exist for the given customer ID
     */
    @Override
    public CustomerRewardSummaryDto calculateRewardsForCustomer(String customerId,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Transaction> customerTransactions = transactionDataService.getAllTransactions().stream()
                .filter(t -> t.getCustomerId().equalsIgnoreCase(customerId))
                .filter(t -> !t.getTransactionDate().isBefore(startDate)
                        && !t.getTransactionDate().isAfter(endDate))
                .collect(Collectors.toList());

        if (customerTransactions.isEmpty()) {
            // Confirm whether the customer exists at all
            boolean customerExists = transactionDataService.getAllTransactions().stream()
                    .anyMatch(t -> t.getCustomerId().equalsIgnoreCase(customerId));
            if (!customerExists) {
                throw new CustomerNotFoundException(customerId);
            }
            // Customer exists but has no transactions in this period
            return CustomerRewardSummaryDto.builder()
                    .customerId(customerId)
                    .customerName(customerId)
                    .monthlyRewards(List.of())
                    .totalPoints(0L)
                    .build();
        }

        return buildCustomerSummary(customerId, customerTransactions);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Only the integer (floor) portion of the amount is used when applying
     * the point thresholds, consistent with the assignment specification.
     */
    @Override
    public long calculatePointsForAmount(double amount) {
        if (amount <= 0) {
            return 0L;
        }

        long points = 0;
        long amountInt = (long) amount; // use floor value per spec

        if (amountInt > UPPER_THRESHOLD) {
            points += (amountInt - (long) UPPER_THRESHOLD) * 2;
            points += (long) (UPPER_THRESHOLD - LOWER_THRESHOLD); // full $50–$100 band = 50 pts
        } else if (amountInt > LOWER_THRESHOLD) {
            points += amountInt - (long) LOWER_THRESHOLD;
        }

        return points;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link CustomerRewardSummaryDto} from a list of transactions
     * belonging to a single customer.
     *
     * @param customerId   the customer identifier
     * @param transactions the customer's transactions (must be non-empty)
     * @return the assembled summary DTO
     */
    private CustomerRewardSummaryDto buildCustomerSummary(String customerId,
                                                           List<Transaction> transactions) {
        String customerName = transactions.get(0).getCustomerName() != null
                ? transactions.get(0).getCustomerName()
                : customerId;

        // Group by year-month and sum points
        Map<String, Long> pointsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getYear()
                                + "-" + t.getTransactionDate().getMonthValue(),
                        Collectors.summingLong(t ->
                                calculatePointsForAmount(t.getAmount().doubleValue()))));

        List<MonthlyRewardDto> monthlyRewards = pointsByMonth.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    int year = Integer.parseInt(parts[0]);
                    int monthValue = Integer.parseInt(parts[1]);
                    return MonthlyRewardDto.builder()
                            .year(year)
                            .month(monthValue)
                            .monthName(Month.of(monthValue).name())
                            .points(e.getValue())
                            .build();
                })
                .sorted(Comparator.comparingInt(MonthlyRewardDto::getYear)
                        .thenComparingInt(MonthlyRewardDto::getMonth))
                .collect(Collectors.toList());

        long totalPoints = monthlyRewards.stream()
                .mapToLong(MonthlyRewardDto::getPoints)
                .sum();

        return CustomerRewardSummaryDto.builder()
                .customerId(customerId)
                .customerName(customerName)
                .monthlyRewards(monthlyRewards)
                .totalPoints(totalPoints)
                .build();
    }

    /**
     * Validates that the supplied date range is logically consistent.
     *
     * @param startDate the range start date
     * @param endDate   the range end date
     * @throws InvalidDateRangeException if either date is null or start is after end
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "Start date '" + startDate + "' must not be after end date '" + endDate + "'");
        }
    }
}
