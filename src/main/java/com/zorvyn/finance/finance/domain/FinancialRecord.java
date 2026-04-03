package com.zorvyn.finance.finance.domain;

import com.zorvyn.finance.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Financial record entity representing a single income or expense transaction.
 *
 * Design decisions:
 * - BigDecimal for amount (never float/double for money)
 * - LocalDate for transaction date (time component not needed for daily records)
 * - Soft delete via BaseEntity.deletedAt
 * - Optimistic locking via BaseEntity.version
 * - Full audit trail via @CreatedBy/@LastModifiedBy
 */
@Entity
@Table(name = "financial_records",
       indexes = {
           @Index(name = "idx_records_user_id", columnList = "user_id"),
           @Index(name = "idx_records_type", columnList = "type"),
           @Index(name = "idx_records_category", columnList = "category"),
           @Index(name = "idx_records_transaction_date", columnList = "transaction_date"),
           @Index(name = "idx_records_deleted_at", columnList = "deleted_at"),
           @Index(name = "idx_records_composite", columnList = "user_id, type, transaction_date, deleted_at")
       })
@Getter
@Setter
@NoArgsConstructor
public class FinancialRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RecordType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private RecordCategory category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
}
