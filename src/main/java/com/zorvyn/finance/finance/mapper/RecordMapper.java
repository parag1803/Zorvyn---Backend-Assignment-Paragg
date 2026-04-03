package com.zorvyn.finance.finance.mapper;

import com.zorvyn.finance.finance.domain.FinancialRecord;
import com.zorvyn.finance.finance.dto.RecordResponse;
import org.springframework.stereotype.Component;

@Component
public class RecordMapper {

    public RecordResponse toResponse(FinancialRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .description(record.getDescription())
                .notes(record.getNotes())
                .transactionDate(record.getTransactionDate())
                .createdBy(record.getCreatedBy())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .version(record.getVersion())
                .build();
    }
}
