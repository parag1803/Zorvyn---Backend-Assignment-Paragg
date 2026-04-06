package com.zorvyn.finance.analytics.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long totalRecords;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private DashboardSummaryResponse(Builder builder) {
        this.totalIncome = builder.totalIncome;
        this.totalExpenses = builder.totalExpenses;
        this.netBalance = builder.netBalance;
        this.totalRecords = builder.totalRecords;
        this.periodStart = builder.periodStart;
        this.periodEnd = builder.periodEnd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netBalance;
        private long totalRecords;
        private LocalDate periodStart;
        private LocalDate periodEnd;

        public Builder totalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; return this; }
        public Builder totalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; return this; }
        public Builder netBalance(BigDecimal netBalance) { this.netBalance = netBalance; return this; }
        public Builder totalRecords(long totalRecords) { this.totalRecords = totalRecords; return this; }
        public Builder periodStart(LocalDate periodStart) { this.periodStart = periodStart; return this; }
        public Builder periodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; return this; }

        public DashboardSummaryResponse build() { return new DashboardSummaryResponse(this); }
    }
}
