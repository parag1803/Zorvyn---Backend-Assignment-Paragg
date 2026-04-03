package com.zorvyn.finance.analytics.dto;

import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CategoryBreakdownResponse {

    private RecordCategory category;
    private RecordType type;
    private BigDecimal totalAmount;
    private long count;
    private double percentage;
}
