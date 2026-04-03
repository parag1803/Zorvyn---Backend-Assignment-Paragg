package com.zorvyn.finance.finance.dto;

import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
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
}
