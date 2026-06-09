package com.charter.rewards.controller;

import com.charter.rewards.dto.CustomerRewardSummaryDto;
import com.charter.rewards.dto.RewardsResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link RewardsController} REST endpoints.
 *
 * <p>Tests verify HTTP request/response handling, date parameter parsing,
 * error responses, and the full call chain from controller through service layer.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RewardsController Integration Tests")
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/rewards";

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: GET /api/v1/rewards (all customers)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return 200 with rewards for all customers")
    void shouldReturnAllCustomerRewards() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period", notNullValue()))
                .andExpect(jsonPath("$.customers", notNullValue()))
                .andExpect(jsonPath("$.customers", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalTransactions", greaterThan(0)));
    }

    @Test
    @DisplayName("Should include correct fields in all-customer response")
    void shouldIncludeCorrectFieldsInAllCustomersResponse() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers[0].customerId", notNullValue()))
                .andExpect(jsonPath("$.customers[0].customerName", notNullValue()))
                .andExpect(jsonPath("$.customers[0].monthlyRewards", notNullValue()))
                .andExpect(jsonPath("$.customers[0].totalPoints", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should return empty list for future date range")
    void shouldReturnEmptyForFutureRange() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2030-01-01")
                .param("endDate", "2030-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers", hasSize(0)))
                .andExpect(jsonPath("$.totalTransactions", equalTo(0)));
    }

    @Test
    @DisplayName("Should return 400 when start date is after end date")
    void shouldReturn400WhenStartAfterEnd() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2024-03-31")
                .param("endDate", "2024-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @DisplayName("Should handle default date range when no params provided")
    void shouldHandleDefaultDateRange() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period", notNullValue()))
                .andExpect(jsonPath("$.customers", notNullValue()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: GET /api/v1/rewards/{customerId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return 200 for existing customer")
    void shouldReturnRewardsForExistingCustomer() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C001")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", equalTo("C001")))
                .andExpect(jsonPath("$.customerName", notNullValue()))
                .andExpect(jsonPath("$.monthlyRewards", notNullValue()))
                .andExpect(jsonPath("$.totalPoints", greaterThan(0)));
    }

    @Test
    @DisplayName("Should return 404 for non-existent customer")
    void shouldReturn404ForNonExistentCustomer() throws Exception {
        mockMvc.perform(get(BASE_URL + "/INVALID_ID")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", equalTo(404)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.message", containsString("INVALID_ID")));
    }

    @Test
    @DisplayName("Should include correct monthly breakdown for single customer")
    void shouldIncludeMonthlyBreakdown() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C001")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRewards", notNullValue()))
                .andExpect(jsonPath("$.monthlyRewards[0].year", notNullValue()))
                .andExpect(jsonPath("$.monthlyRewards[0].month", notNullValue()))
                .andExpect(jsonPath("$.monthlyRewards[0].monthName", notNullValue()))
                .andExpect(jsonPath("$.monthlyRewards[0].points", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should calculate correct total points from monthly rewards")
    void shouldCalculateCorrectTotalPoints() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/C001")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Verify structure – detailed assertions in service tests
        assertThat(content).contains("\"totalPoints\"");
    }

    @Test
    @DisplayName("Should return empty rewards for customer with no transactions in range")
    void shouldReturnEmptyRewardsForNoTransactionsInRange() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C001")
                .param("startDate", "2030-01-01")
                .param("endDate", "2030-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", equalTo("C001")))
                .andExpect(jsonPath("$.monthlyRewards", hasSize(0)))
                .andExpect(jsonPath("$.totalPoints", equalTo(0)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: GET /api/v1/rewards/calculate
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should calculate points for given amount")
    void shouldCalculatePointsForAmount() throws Exception {
        mockMvc.perform(get(BASE_URL + "/calculate")
                .param("amount", "120.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("90"));
    }

    @Test
    @DisplayName("Should return 0 for amount below $50")
    void shouldReturn0ForAmountBelow50() throws Exception {
        mockMvc.perform(get(BASE_URL + "/calculate")
                .param("amount", "25.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @DisplayName("Should return 50 for exactly $100")
    void shouldReturn50ForExactly100() throws Exception {
        mockMvc.perform(get(BASE_URL + "/calculate")
                .param("amount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    @DisplayName("Should return 400 for zero or negative amount")
    void shouldReturn400ForZeroOrNegativeAmount() throws Exception {
        mockMvc.perform(get(BASE_URL + "/calculate")
                .param("amount", "0.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", equalTo(400)));

        mockMvc.perform(get(BASE_URL + "/calculate")
                .param("amount", "-50.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle fractional amounts correctly (floor)")
    void shouldHandleFractionalAmounts() throws Exception {
        // $99.99 floors to $99 → 49 points
        mockMvc.perform(get(BASE_URL + "/calculate")
                .param("amount", "99.99"))
                .andExpect(status().isOk())
                .andExpect(content().string("49"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: Error handling and validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return 400 for invalid date format")
    void shouldReturn400ForInvalidDateFormat() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "01-01-2024") // Wrong format
                .param("endDate", "2024-03-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return error response with timestamp")
    void shouldIncludeTimestampInErrorResponse() throws Exception {
        mockMvc.perform(get(BASE_URL + "/INVALID")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should return correct status code in error response")
    void shouldReturnCorrectStatusInError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/INVALID")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", equalTo(404)))
                .andExpect(jsonPath("$.error", equalTo("Not Found")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests: Multiple customers with different transaction counts
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should handle customer with high-value transactions")
    void shouldHandleHighValueTransactions() throws Exception {
        // C004 (David Brown) has transactions including $500 and $420
        mockMvc.perform(get(BASE_URL + "/C004")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", equalTo("C004")))
                .andExpect(jsonPath("$.totalPoints", greaterThan(500L)));
    }

    @Test
    @DisplayName("Should handle customer with low-value transactions")
    void shouldHandleLowValueTransactions() throws Exception {
        // C005 (Eva Martinez) includes a $25 transaction (below $50)
        mockMvc.perform(get(BASE_URL + "/C005")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", equalTo("C005")))
                .andExpect(jsonPath("$.monthlyRewards", notNullValue()));
    }

    @Test
    @DisplayName("Should correctly aggregate across three months")
    void shouldAggregateAcrossThreeMonths() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C001")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$.monthlyRewards[?(@.month == 1)]", hasSize(1)))
                .andExpect(jsonPath("$.monthlyRewards[?(@.month == 2)]", hasSize(1)))
                .andExpect(jsonPath("$.monthlyRewards[?(@.month == 3)]", hasSize(1)));
    }
}
