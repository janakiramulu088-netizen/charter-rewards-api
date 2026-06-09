package com.charter.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top-level API response wrapper for the rewards calculation endpoint.
 *
 * <p>Contains a list of reward summaries for all customers within the requested period,
 * along with metadata describing the period queried.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardsResponseDto {

    /**
     * Human-readable description of the calculation period
     * (e.g., "2024-01 to 2024-03").
     */
    private String period;

    /**
     * Reward summaries for each customer in the period.
     */
    private List<CustomerRewardSummaryDto> customers;

    /**
     * Total number of transactions processed.
     */
    private int totalTransactions;
}
