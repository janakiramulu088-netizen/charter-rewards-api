package com.charter.rewards.service;

import com.charter.rewards.dto.CustomerRewardSummaryDto;
import com.charter.rewards.dto.RewardsResponseDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service contract for calculating customer reward points.
 *
 * <p>Provides methods to compute reward summaries across all customers
 * or for a specific customer, within a given date range.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
public interface RewardsService {

    /**
     * Calculates reward points for all customers within the specified date range.
     *
     * @param startDate the inclusive start date of the period
     * @param endDate   the inclusive end date of the period
     * @return a {@link RewardsResponseDto} containing per-customer, per-month summaries
     */
    RewardsResponseDto calculateRewardsForAllCustomers(LocalDate startDate, LocalDate endDate);

    /**
     * Calculates reward points for a single customer within the specified date range.
     *
     * @param customerId the unique customer identifier
     * @param startDate  the inclusive start date of the period
     * @param endDate    the inclusive end date of the period
     * @return a {@link CustomerRewardSummaryDto} for the specified customer
     */
    CustomerRewardSummaryDto calculateRewardsForCustomer(String customerId, LocalDate startDate, LocalDate endDate);

    /**
     * Computes the reward points earned for a single transaction amount.
     *
     * <p>Points are awarded as follows:
     * <ul>
     *   <li>2 points per dollar spent over $100</li>
     *   <li>1 point per dollar spent between $50 and $100</li>
     *   <li>0 points for amounts $50 and below</li>
     * </ul>
     *
     * @param amount the transaction amount in USD (only the integer portion is used)
     * @return the number of reward points earned
     */
    long calculatePointsForAmount(double amount);
}
