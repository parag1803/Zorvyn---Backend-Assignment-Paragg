package com.zorvyn.finance.analytics.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlyTrendResponse {

    private int year;
    private int month;
    private String monthName;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal net;
}
