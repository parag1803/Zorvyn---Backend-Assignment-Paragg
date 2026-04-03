package com.zorvyn.finance.finance.specification;

import com.zorvyn.finance.finance.domain.FinancialRecord;
import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import com.zorvyn.finance.finance.dto.RecordFilterCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA Specification builder for dynamic, composable financial record filtering.
 * 
 * Uses the Criteria API under the hood — no string concatenation, no SQL injection risk.
 * All filters are optional and combinable (AND logic).
 */
public final class RecordSpecification {

    private RecordSpecification() {}

    /**
     * Build a composite specification from filter criteria.
     */
    public static Specification<FinancialRecord> fromCriteria(RecordFilterCriteria criteria) {
        Specification<FinancialRecord> spec = notDeleted();

        if (criteria.getType() != null) {
            spec = spec.and(hasType(criteria.getType()));
        }
        if (criteria.getCategory() != null) {
            spec = spec.and(hasCategory(criteria.getCategory()));
        }
        if (criteria.getStartDate() != null) {
            spec = spec.and(dateAfter(criteria.getStartDate()));
        }
        if (criteria.getEndDate() != null) {
            spec = spec.and(dateBefore(criteria.getEndDate()));
        }
        if (criteria.getMinAmount() != null) {
            spec = spec.and(amountGreaterThan(criteria.getMinAmount()));
        }
        if (criteria.getMaxAmount() != null) {
            spec = spec.and(amountLessThan(criteria.getMaxAmount()));
        }
        if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
            spec = spec.and(searchText(criteria.getSearch().trim()));
        }

        return spec;
    }

    public static Specification<FinancialRecord> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<FinancialRecord> hasType(RecordType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> hasCategory(RecordCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<FinancialRecord> dateAfter(LocalDate startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate);
    }

    public static Specification<FinancialRecord> dateBefore(LocalDate endDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), endDate);
    }

    public static Specification<FinancialRecord> amountGreaterThan(BigDecimal minAmount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<FinancialRecord> amountLessThan(BigDecimal maxAmount) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

    public static Specification<FinancialRecord> searchText(String search) {
        return (root, query, cb) -> {
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("notes")), pattern)
            );
        };
    }
}
