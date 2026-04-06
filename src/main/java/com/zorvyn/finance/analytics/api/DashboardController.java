package com.zorvyn.finance.analytics.api;

import com.zorvyn.finance.analytics.application.DashboardService;
import com.zorvyn.finance.analytics.dto.CategoryBreakdownResponse;
import com.zorvyn.finance.analytics.dto.DashboardSummaryResponse;
import com.zorvyn.finance.analytics.dto.MonthlyTrendResponse;
import com.zorvyn.finance.finance.dto.RecordResponse;
import com.zorvyn.finance.shared.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(
    name = "Dashboard & Analytics",
    description = "Aggregated financial analytics for the dashboard.\n\n" +
                  "**RBAC:**\n" +
                  "- `summary` and `recent-activity` → all authenticated users (VIEWER, ANALYST, ADMIN)\n" +
                  "- `category-breakdown` and `monthly-trends` → ANALYST + ADMIN only\n\n" +
                  "**Performance:** Results are cached with a 5-minute TTL (Caffeine). " +
                  "Cache is automatically evicted when records are created, updated, or deleted.\n\n" +
                  "**Queries:** All aggregations use JPQL aggregate queries — no Java-side loops."
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get dashboard summary",
        description = "Returns total income, total expenses, net balance, and record count for the given period. " +
                      "Accessible to all authenticated users. Results are cached for 5 minutes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format — use yyyy-MM-dd",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<DashboardSummaryResponse>> getSummary(
            @Parameter(description = "Start of the reporting period (yyyy-MM-dd). Defaults to all-time.", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End of the reporting period (yyyy-MM-dd). Defaults to all-time.", example = "2024-12-31")
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        DashboardSummaryResponse summary = dashboardService.getSummary(start, end);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(summary, "Dashboard summary retrieved"));
    }

    @GetMapping("/category-breakdown")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary = "Get category breakdown",
        description = "Returns income and expense totals grouped by category, with percentage share of total. " +
                      "**ANALYST + ADMIN only.** Results are cached for 5 minutes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category breakdown retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ANALYST or ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<List<CategoryBreakdownResponse>>> getCategoryBreakdown(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2024-12-31")
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        List<CategoryBreakdownResponse> breakdown = dashboardService.getCategoryBreakdown(start, end);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(breakdown, "Category breakdown retrieved"));
    }

    @GetMapping("/monthly-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary = "Get monthly trends",
        description = "Returns month-by-month income, expense, and net balance for the last N months. " +
                      "**ANALYST + ADMIN only.** Useful for charting historical trends."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Monthly trends retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid months value — must be positive integer",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ANALYST or ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @Parameter(description = "Number of months to look back from today (default: 12, max: 60)", example = "12")
            @RequestParam(defaultValue = "12") int months) {

        List<MonthlyTrendResponse> trends = dashboardService.getMonthlyTrends(months);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(trends, "Monthly trends retrieved"));
    }

    @GetMapping("/recent-activity")
    @Operation(
        summary = "Get recent activity",
        description = "Returns the most recent financial transactions. Accessible to all authenticated users. " +
                      "Maximum of 50 records per request."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recent activity retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<List<RecordResponse>>> getRecentActivity(
            @Parameter(description = "Number of recent records to return (default: 10, max: 50)", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        limit = Math.min(limit, 50);
        List<RecordResponse> activity = dashboardService.getRecentActivity(limit);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(activity, "Recent activity retrieved"));
    }
}
