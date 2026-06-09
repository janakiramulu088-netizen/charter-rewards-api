package com.charter.rewards.controller;

import com.charter.rewards.dto.CustomerRewardSummaryDto;
import com.charter.rewards.dto.RewardsResponseDto;
import com.charter.rewards.exception.InvalidDateRangeException;
import com.charter.rewards.service.RewardsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller exposing endpoints for the Customer Rewards API.
 *
 * <p>Base path: {@code /api/v1/rewards}
 *
 * <p>All endpoints accept optional {@code startDate} and {@code endDate} query parameters
 * (ISO-8601 format, e.g. {@code 2024-01-01}). When omitted, a default three-month
 * window ending today is applied automatically.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/rewards")
@Tag(name = "Customer Rewards", description = "APIs for calculating customer reward points based on purchase transactions")
public class RewardsController {

    private final RewardsService rewardsService;

    /**
     * Constructs the controller with the required rewards service.
     *
     * @param rewardsService the service used to compute reward points
     */
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Retrieves reward summaries for all customers within the specified date range.
     *
     * <p>If no date range is supplied, defaults to the last three calendar months
     * (first day of month three months ago through today).
     *
     * @param startDate optional inclusive start date in {@code yyyy-MM-dd} format
     * @param endDate   optional inclusive end date in {@code yyyy-MM-dd} format
     * @return HTTP 200 with a {@link RewardsResponseDto} for all customers
     */
    @GetMapping
    @Operation(
            summary = "Get reward points for all customers",
            description = "Returns monthly and total reward point summaries for every customer "
                    + "whose transactions fall within the specified date range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rewards calculated successfully",
                    content = @Content(schema = @Schema(implementation = RewardsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range supplied")
    })
    public ResponseEntity<RewardsResponseDto> getAllCustomerRewards(
            @Parameter(description = "Inclusive start date (yyyy-MM-dd). Defaults to 3 months ago.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Inclusive end date (yyyy-MM-dd). Defaults to today.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate[] range = resolveDefaultDateRange(startDate, endDate);
        RewardsResponseDto response = rewardsService.calculateRewardsForAllCustomers(range[0], range[1]);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the reward summary for a single customer within the specified date range.
     *
     * <p>If no date range is supplied, defaults to the last three calendar months.
     *
     * @param customerId the unique customer identifier (path variable)
     * @param startDate  optional inclusive start date in {@code yyyy-MM-dd} format
     * @param endDate    optional inclusive end date in {@code yyyy-MM-dd} format
     * @return HTTP 200 with a {@link CustomerRewardSummaryDto} for the customer
     */
    @GetMapping("/{customerId}")
    @Operation(
            summary = "Get reward points for a specific customer",
            description = "Returns monthly and total reward point summaries for a single customer "
                    + "identified by their customer ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rewards calculated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerRewardSummaryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range supplied"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerRewardSummaryDto> getCustomerRewards(
            @Parameter(description = "Unique customer identifier", required = true)
            @PathVariable String customerId,

            @Parameter(description = "Inclusive start date (yyyy-MM-dd). Defaults to 3 months ago.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Inclusive end date (yyyy-MM-dd). Defaults to today.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate[] range = resolveDefaultDateRange(startDate, endDate);
        CustomerRewardSummaryDto response =
                rewardsService.calculateRewardsForCustomer(customerId, range[0], range[1]);
        return ResponseEntity.ok(response);
    }

    /**
     * Calculates the reward points earned for a single transaction amount.
     *
     * <p>Useful for ad-hoc point queries without referencing stored transactions.
     *
     * @param amount the transaction amount in USD (must be positive)
     * @return HTTP 200 with the computed points as a plain long value
     */
    @GetMapping("/calculate")
    @Operation(
            summary = "Calculate points for a given transaction amount",
            description = "Returns the reward points that would be earned for a single transaction "
                    + "of the specified dollar amount."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Points calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount supplied")
    })
    public ResponseEntity<Long> calculatePointsForAmount(
            @Parameter(description = "Transaction amount in USD (must be > 0)", required = true)
            @RequestParam double amount) {

        if (amount <= 0) {
            throw new InvalidDateRangeException("Amount must be greater than zero");
        }
        long points = rewardsService.calculatePointsForAmount(amount);
        return ResponseEntity.ok(points);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the supplied dates if both are present; otherwise applies a default
     * three-month window ending today.
     *
     * @param startDate caller-supplied start date (may be null)
     * @param endDate   caller-supplied end date (may be null)
     * @return two-element array {@code [resolvedStart, resolvedEnd]}
     */
    private LocalDate[] resolveDefaultDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEnd   = endDate   != null ? endDate   : LocalDate.now();
        LocalDate resolvedStart = startDate != null ? startDate : resolvedEnd.minusMonths(3).withDayOfMonth(1);
        return new LocalDate[]{resolvedStart, resolvedEnd};
    }
}
