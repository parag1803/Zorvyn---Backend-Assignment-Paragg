package com.zorvyn.finance.finance.dto;

import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class RecordResponse {

    private UUID id;
    private BigDecimal amount;
    private RecordType type;
    private RecordCategory category;
    private String description;
    private String notes;
    private LocalDate transactionDate;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private RecordResponse(Builder builder) {
        this.id = builder.id;
        this.amount = builder.amount;
        this.type = builder.type;
        this.category = builder.category;
        this.description = builder.description;
        this.notes = builder.notes;
        this.transactionDate = builder.transactionDate;
        this.createdBy = builder.createdBy;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private BigDecimal amount;
        private RecordType type;
        private RecordCategory category;
        private String description;
        private String notes;
        private LocalDate transactionDate;
        private UUID createdBy;
        private Instant createdAt;
        private Instant updatedAt;
        private Long version;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder type(RecordType type) { this.type = type; return this; }
        public Builder category(RecordCategory category) { this.category = category; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public Builder createdBy(UUID createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder version(Long version) { this.version = version; return this; }

        public RecordResponse build() { return new RecordResponse(this); }
    }
}
