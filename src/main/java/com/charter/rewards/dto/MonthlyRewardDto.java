package com.charter.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the reward points earned by a customer
 * in a specific calendar month.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRewardDto {

    /**
     * The year of the reward period (e.g., 2024).
     */
    private int year;

    /**
     * The month of the reward period (1 = January, 12 = December).
     */
    private int month;

    /**
     * Human-readable month name (e.g., "JANUARY").
     */
    private String monthName;

    /**
     * Total reward points earned in this month.
     */
    private long points;
}
