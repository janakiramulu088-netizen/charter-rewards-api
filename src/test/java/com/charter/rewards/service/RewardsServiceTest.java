package com.charter.rewards.service;

import com.charter.rewards.dto.CustomerRewardSummaryDto;
import com.charter.rewards.dto.MonthlyRewardDto;
import com.charter.rewards.dto.RewardsResponseDto;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.exception.InvalidDateRangeException;
import com.charter.rewards.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the {@link RewardsService} interface and its default implementation.
 *
 * <p>Tests cover reward point calculation rules, date range validation,
 * customer lookup, and monthly/total aggregation logic.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@SpringBootTest
@DisplayName("RewardsService Tests")
class RewardsServiceTest {

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private TransactionDataService transactionDataService;

    @BeforeEach
    void setup() {
        assertThat(transactionDataService).isNotNull();
        assertThat(rewardsService).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: calculatePointsForAmount
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should award 0 points for amount <= $50")
    void shouldAwardZeroPointsForAmountBelowFifty() {
        assertThat(rewardsService.calculatePointsForAmount(0.0)).isEqualTo(0L);
        assertThat(rewardsService.calculatePointsForAmount(25.0)).isEqualTo(0L);
        assertThat(rewardsService.calculatePointsForAmount(50.0)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should award 1 point per dollar between $50 and $100")
    void shouldAward1PointPerDollarBetween50And100() {
        // $51 → 1 point
        assertThat(rewardsService.calculatePointsForAmount(51.0)).isEqualTo(1L);
        // $75 → 25 points
        assertThat(rewardsService.calculatePointsForAmount(75.0)).isEqualTo(25L);
        // $100 → 50 points (entire $50–$100 band)
        assertThat(rewardsService.calculatePointsForAmount(100.0)).isEqualTo(50L);
    }

    @Test
    @DisplayName("Should award 2 points per dollar over $100")
    void shouldAward2PointsPerDollarOver100() {
        // $101 → 50 (for $50–$100) + 2 (for $1 over) = 52
        assertThat(rewardsService.calculatePointsForAmount(101.0)).isEqualTo(52L);
        // $120 → 50 (for $50–$100) + 40 (for $20 over) = 90
        assertThat(rewardsService.calculatePointsForAmount(120.0)).isEqualTo(90L);
    }

    @ParameterizedTest
    @CsvSource({
            "25.99,   0",      // Below $50
            "50.99,  1",       // Just above $50
            "75.50, 25",       // Mid-band
            "99.99, 49",       // Just below $100 (floor to $99)
            "100.00, 50",      // Exactly $100
            "120.00, 90",      // Assignment example: 2*20 + 1*50 = 90
            "150.00, 150",     // 2*50 + 1*50 = 150
            "200.00, 300",     // 2*100 + 1*50 = 300
    })
    @DisplayName("Calculate points for various amounts")
    void calculatePointsParameterized(double amount, long expectedPoints) {
        assertThat(rewardsService.calculatePointsForAmount(amount))
                .as("Points for $%.2f", amount)
                .isEqualTo(expectedPoints);
    }

    @Test
    @DisplayName("Should ignore fractional cents in calculation (use floor)")
    void shouldUseFloorForFractionalCents() {
        // Both $99.99 and $99.01 floor to $99 → 49 points
        assertThat(rewardsService.calculatePointsForAmount(99.99)).isEqualTo(49L);
        assertThat(rewardsService.calculatePointsForAmount(99.01)).isEqualTo(49L);
    }

    @Test
    @DisplayName("Should return 0 for negative amounts")
    void shouldReturnZeroForNegativeAmount() {
        assertThat(rewardsService.calculatePointsForAmount(-50.0)).isEqualTo(0L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: calculateRewardsForCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should retrieve rewards for existing customer")
    void shouldRetrieveRewardsForExistingCustomer() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        CustomerRewardSummaryDto summary = rewardsService
                .calculateRewardsForCustomer("C001", startDate, endDate);

        assertThat(summary).isNotNull();
        assertThat(summary.getCustomerId()).isEqualTo("C001");
        assertThat(summary.getCustomerName()).isEqualTo("Alice Johnson");
        assertThat(summary.getMonthlyRewards()).isNotEmpty();
        assertThat(summary.getTotalPoints()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException for non-existent customer")
    void shouldThrowExceptionForNonExistentCustomer() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        assertThatThrownBy(() ->
                rewardsService.calculateRewardsForCustomer("INVALID_ID", startDate, endDate))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("INVALID_ID");
    }

    @Test
    @DisplayName("Should return empty rewards when customer has no transactions in date range")
    void shouldReturnEmptyRewardsForNoTransactionsInRange() {
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);

        // Customer exists but has no transactions in future date range
        CustomerRewardSummaryDto summary = rewardsService
                .calculateRewardsForCustomer("C001", startDate, endDate);

        assertThat(summary.getMonthlyRewards()).isEmpty();
        assertThat(summary.getTotalPoints()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should validate start date not after end date")
    void shouldValidateDateOrder() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 1, 1);

        assertThatThrownBy(() ->
                rewardsService.calculateRewardsForCustomer("C001", start, end))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("must not be after");
    }

    @Test
    @DisplayName("Should validate null dates")
    void shouldValidateNullDates() {
        assertThatThrownBy(() ->
                rewardsService.calculateRewardsForCustomer("C001", null, LocalDate.now()))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("must not be null");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: calculateRewardsForAllCustomers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should retrieve rewards for all customers in date range")
    void shouldRetrieveRewardsForAllCustomers() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        RewardsResponseDto response = rewardsService
                .calculateRewardsForAllCustomers(startDate, endDate);

        assertThat(response).isNotNull();
        assertThat(response.getCustomers()).isNotEmpty();
        assertThat(response.getTotalTransactions()).isGreaterThan(0);
        assertThat(response.getPeriod()).isNotBlank();
    }

    @Test
    @DisplayName("Should include all five customers in sample dataset")
    void shouldIncludeAllCustomersInResponse() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        RewardsResponseDto response = rewardsService
                .calculateRewardsForAllCustomers(startDate, endDate);

        assertThat(response.getCustomers())
                .extracting(CustomerRewardSummaryDto::getCustomerId)
                .containsExactlyInAnyOrder("C001", "C002", "C003", "C004", "C005");
    }

    @Test
    @DisplayName("Should correctly sum points across multiple months")
    void shouldSumPointsAcrossMultipleMonths() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        CustomerRewardSummaryDto summary = rewardsService
                .calculateRewardsForCustomer("C001", startDate, endDate);

        long monthlySum = summary.getMonthlyRewards().stream()
                .mapToLong(MonthlyRewardDto::getPoints)
                .sum();

        assertThat(summary.getTotalPoints()).isEqualTo(monthlySum);
    }

    @Test
    @DisplayName("Should return empty list for date range with no transactions")
    void shouldReturnEmptyForNoTransactionsInRange() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        RewardsResponseDto response = rewardsService
                .calculateRewardsForAllCustomers(startDate, endDate);

        assertThat(response.getCustomers()).isEmpty();
        assertThat(response.getTotalTransactions()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should filter transactions by date range correctly")
    void shouldFilterByDateRangeCorrectly() {
        // Only January
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        RewardsResponseDto response = rewardsService
                .calculateRewardsForAllCustomers(startDate, endDate);

        assertThat(response.getTotalTransactions()).isEqualTo(5); // 5 Jan transactions in dataset
        response.getCustomers().forEach(customer -> {
            customer.getMonthlyRewards().forEach(monthly -> {
                assertThat(monthly.getMonth()).isEqualTo(1);
                assertThat(monthly.getYear()).isEqualTo(2024);
            });
        });
    }

    @Test
    @DisplayName("Should handle single-day date range")
    void shouldHandleSingleDayRange() {
        LocalDate date = LocalDate.of(2024, 1, 5); // Alice's first transaction

        RewardsResponseDto response = rewardsService
                .calculateRewardsForAllCustomers(date, date);

        assertThat(response.getTotalTransactions()).isEqualTo(1);
    }
}
