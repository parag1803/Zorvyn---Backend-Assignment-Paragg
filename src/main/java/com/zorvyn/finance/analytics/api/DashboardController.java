package com.zorvyn.finance.analytics.api;

import com.zorvyn.finance.analytics.application.DashboardService;
import com.zorvyn.finance.analytics.dto.CategoryBreakdownResponse;
import com.zorvyn.finance.analytics.dto.DashboardSummaryResponse;
import com.zorvyn.finance.analytics.dto.MonthlyTrendResponse;
import com.zorvyn.finance.finance.dto.RecordResponse;
import com.zorvyn.finance.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard & Analytics", description = "Aggregated financial data for dashboard visualizations")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary",
               description = "Total income, expenses, net balance, and record count. Accessible to all authenticated users.")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        DashboardSummaryResponse summary = dashboardService.getSummary(start, end);
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved"));
    }

    @GetMapping("/category-breakdown")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get category breakdown",
               description = "Income and expense totals grouped by category with percentages (Analyst / Admin)")
    public ResponseEntity<ApiResponse<List<CategoryBreakdownResponse>>> getCategoryBreakdown(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        List<CategoryBreakdownResponse> breakdown = dashboardService.getCategoryBreakdown(start, end);
        return ResponseEntity.ok(ApiResponse.success(breakdown, "Category breakdown retrieved"));
    }

    @GetMapping("/monthly-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get monthly trends",
               description = "Monthly income, expense, and net trends (Analyst / Admin)")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @Parameter(description = "Number of months to look back (default: 12)")
            @RequestParam(defaultValue = "12") int months) {

        List<MonthlyTrendResponse> trends = dashboardService.getMonthlyTrends(months);
        return ResponseEntity.ok(ApiResponse.success(trends, "Monthly trends retrieved"));
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent activity",
               description = "Latest financial transactions. Accessible to all authenticated users.")
    public ResponseEntity<ApiResponse<List<RecordResponse>>> getRecentActivity(
            @Parameter(description = "Number of recent records (default: 10, max: 50)")
            @RequestParam(defaultValue = "10") int limit) {

        limit = Math.min(limit, 50);
        List<RecordResponse> activity = dashboardService.getRecentActivity(limit);
        return ResponseEntity.ok(ApiResponse.success(activity, "Recent activity retrieved"));
    }
}
