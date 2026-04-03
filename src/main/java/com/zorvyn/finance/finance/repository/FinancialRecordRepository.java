package com.zorvyn.finance.finance.repository;

import com.zorvyn.finance.finance.domain.FinancialRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID>,
        JpaSpecificationExecutor<FinancialRecord> {

    @Query("SELECT r FROM FinancialRecord r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<FinancialRecord> findActiveById(@Param("id") UUID id);

    @Query("SELECT r FROM FinancialRecord r WHERE r.deletedAt IS NULL ORDER BY r.transactionDate DESC, r.createdAt DESC")
    Page<FinancialRecord> findAllActive(Pageable pageable);

    // ── Analytics Queries (CQRS-Lite: optimized read-side) ─────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.type = 'INCOME' AND r.deletedAt IS NULL " +
           "AND (:startDate IS NULL OR r.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.transactionDate <= :endDate)")
    BigDecimal calculateTotalIncome(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.type = 'EXPENSE' AND r.deletedAt IS NULL " +
           "AND (:startDate IS NULL OR r.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.transactionDate <= :endDate)")
    BigDecimal calculateTotalExpenses(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(r) FROM FinancialRecord r WHERE r.deletedAt IS NULL " +
           "AND (:startDate IS NULL OR r.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.transactionDate <= :endDate)")
    long countActiveRecords(@Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    @Query("SELECT r.category AS category, " +
           "r.type AS type, " +
           "SUM(r.amount) AS total, " +
           "COUNT(r) AS count " +
           "FROM FinancialRecord r WHERE r.deletedAt IS NULL " +
           "AND (:startDate IS NULL OR r.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.transactionDate <= :endDate) " +
           "GROUP BY r.category, r.type " +
           "ORDER BY total DESC")
    List<Object[]> getCategoryBreakdown(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT YEAR(r.transactionDate) AS year, " +
           "MONTH(r.transactionDate) AS month, " +
           "r.type AS type, " +
           "SUM(r.amount) AS total " +
           "FROM FinancialRecord r WHERE r.deletedAt IS NULL " +
           "AND r.transactionDate >= :startDate " +
           "GROUP BY YEAR(r.transactionDate), MONTH(r.transactionDate), r.type " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyTrends(@Param("startDate") LocalDate startDate);

    @Query("SELECT r FROM FinancialRecord r WHERE r.deletedAt IS NULL " +
           "ORDER BY r.transactionDate DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);
}
