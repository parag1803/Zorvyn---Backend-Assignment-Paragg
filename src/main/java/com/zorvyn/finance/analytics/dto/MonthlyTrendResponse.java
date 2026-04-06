package com.zorvyn.finance.analytics.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MonthlyTrendResponse {

    private int year;
    private int month;
    private String monthName;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal net;

    private MonthlyTrendResponse(Builder builder) {
        this.year = builder.year;
        this.month = builder.month;
        this.monthName = builder.monthName;
        this.income = builder.income;
        this.expenses = builder.expenses;
        this.net = builder.net;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int year;
        private int month;
        private String monthName;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;

        public Builder year(int year) { this.year = year; return this; }
        public Builder month(int month) { this.month = month; return this; }
        public Builder monthName(String monthName) { this.monthName = monthName; return this; }
        public Builder income(BigDecimal income) { this.income = income; return this; }
        public Builder expenses(BigDecimal expenses) { this.expenses = expenses; return this; }
        public Builder net(BigDecimal net) { this.net = net; return this; }

        public MonthlyTrendResponse build() { return new MonthlyTrendResponse(this); }
    }
}
