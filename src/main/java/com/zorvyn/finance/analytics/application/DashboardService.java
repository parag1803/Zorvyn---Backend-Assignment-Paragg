package com.zorvyn.finance.analytics.application;

import com.zorvyn.finance.analytics.dto.CategoryBreakdownResponse;
import com.zorvyn.finance.analytics.dto.DashboardSummaryResponse;
import com.zorvyn.finance.analytics.dto.MonthlyTrendResponse;
import com.zorvyn.finance.finance.domain.FinancialRecord;
import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import com.zorvyn.finance.finance.dto.RecordResponse;
import com.zorvyn.finance.finance.mapper.RecordMapper;
import com.zorvyn.finance.finance.repository.FinancialRecordRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard analytics service providing aggregated financial data.
 * 
 * Implements CQRS-Lite: uses optimized JPQL aggregate queries directly
 * against the database rather than loading entities into memory.
 * Results are cached with Caffeine (5-minute TTL).
 */
@Service
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final RecordMapper recordMapper;

    public DashboardService(FinancialRecordRepository recordRepository,
                             RecordMapper recordMapper) {
        this.recordRepository = recordRepository;
        this.recordMapper = recordMapper;
    }

    /**
     * Get dashboard summary: total income, expenses, net balance, record count.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardSummary", key = "#startDate + '-' + #endDate")
    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = recordRepository.calculateTotalIncome(startDate, endDate);
        BigDecimal totalExpenses = recordRepository.calculateTotalExpenses(startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        long totalRecords = recordRepository.countActiveRecords(startDate, endDate);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome.setScale(2, RoundingMode.HALF_UP))
                .totalExpenses(totalExpenses.setScale(2, RoundingMode.HALF_UP))
                .netBalance(netBalance.setScale(2, RoundingMode.HALF_UP))
                .totalRecords(totalRecords)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }

    /**
     * Get category-wise breakdown with percentages.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryBreakdown", key = "#startDate + '-' + #endDate")
    public List<CategoryBreakdownResponse> getCategoryBreakdown(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = recordRepository.getCategoryBreakdown(startDate, endDate);

        // Calculate grand total for percentage computation
        BigDecimal grandTotal = results.stream()
                .map(r -> (BigDecimal) r[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream().map(row -> {
            BigDecimal total = (BigDecimal) row[2];
            double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                    : 0.0;

            return CategoryBreakdownResponse.builder()
                    .category((RecordCategory) row[0])
                    .type((RecordType) row[1])
                    .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                    .count((Long) row[3])
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build();
        }).toList();
    }

    /**
     * Get monthly income/expense trends for the last N months.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "monthlyTrends", key = "#months")
    public List<MonthlyTrendResponse> getMonthlyTrends(int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months).withDayOfMonth(1);
        List<Object[]> results = recordRepository.getMonthlyTrends(startDate);

        // Group by year-month
        Map<String, Map<String, BigDecimal>> monthlyData = new LinkedHashMap<>();
        for (Object[] row : results) {
            int year = (Integer) row[0];
            int month = (Integer) row[1];
            String key = year + "-" + String.format("%02d", month);
            RecordType type = (RecordType) row[2];
            BigDecimal total = (BigDecimal) row[3];

            monthlyData.computeIfAbsent(key, k -> new HashMap<>());
            monthlyData.get(key).put(type.name(), total);
        }

        // Build response with income, expense, and net for each month
        return monthlyData.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            Map<String, BigDecimal> data = entry.getValue();

            BigDecimal income = data.getOrDefault("INCOME", BigDecimal.ZERO);
            BigDecimal expenses = data.getOrDefault("EXPENSE", BigDecimal.ZERO);

            return MonthlyTrendResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(Month.of(month).name())
                    .income(income.setScale(2, RoundingMode.HALF_UP))
                    .expenses(expenses.setScale(2, RoundingMode.HALF_UP))
                    .net(income.subtract(expenses).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }).toList();
    }

    /**
     * Get recent financial activity (latest N records).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "recentActivity", key = "#limit")
    public List<RecordResponse> getRecentActivity(int limit) {
        List<FinancialRecord> records = recordRepository.findRecentActivity(PageRequest.of(0, limit));
        return records.stream().map(recordMapper::toResponse).toList();
    }
}
