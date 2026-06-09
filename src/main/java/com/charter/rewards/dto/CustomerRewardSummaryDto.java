package com.charter.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing the complete reward summary for a single customer,
 * including a breakdown by month and the overall total points earned.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewardSummaryDto {

    /**
     * Unique identifier of the customer.
     */
    private String customerId;

    /**
     * Display name of the customer.
     */
    private String customerName;

    /**
     * List of reward points earned per month, sorted chronologically.
     */
    private List<MonthlyRewardDto> monthlyRewards;

    /**
     * Total reward points earned across all months in the period.
     */
    private long totalPoints;
}
