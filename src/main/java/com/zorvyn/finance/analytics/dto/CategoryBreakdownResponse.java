package com.zorvyn.finance.analytics.dto;

import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategoryBreakdownResponse {

    private RecordCategory category;
    private RecordType type;
    private BigDecimal totalAmount;
    private long count;
    private double percentage;

    private CategoryBreakdownResponse(Builder builder) {
        this.category = builder.category;
        this.type = builder.type;
        this.totalAmount = builder.totalAmount;
        this.count = builder.count;
        this.percentage = builder.percentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RecordCategory category;
        private RecordType type;
        private BigDecimal totalAmount;
        private long count;
        private double percentage;

        public Builder category(RecordCategory category) { this.category = category; return this; }
        public Builder type(RecordType type) { this.type = type; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder count(long count) { this.count = count; return this; }
        public Builder percentage(double percentage) { this.percentage = percentage; return this; }

        public CategoryBreakdownResponse build() { return new CategoryBreakdownResponse(this); }
    }
}
