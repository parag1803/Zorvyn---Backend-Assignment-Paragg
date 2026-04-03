package com.zorvyn.finance.finance.dto;

import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Query parameter DTO for filtering financial records.
 * All fields are optional — null means "no filter on this field".
 */
@Getter
@Setter
public class RecordFilterCriteria {

    private RecordType type;
    private RecordCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String search;
}
