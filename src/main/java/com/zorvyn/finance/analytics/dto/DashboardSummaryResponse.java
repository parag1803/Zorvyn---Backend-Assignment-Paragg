package com.zorvyn.finance.analytics.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long totalRecords;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
