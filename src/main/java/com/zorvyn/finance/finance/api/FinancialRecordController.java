package com.zorvyn.finance.finance.api;

import com.zorvyn.finance.finance.application.FinancialRecordService;
import com.zorvyn.finance.finance.dto.*;
import com.zorvyn.finance.shared.dto.ApiResponse;
import com.zorvyn.finance.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/records")
@Tag(name = "Financial Records", description = "CRUD operations for financial transactions")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    public FinancialRecordController(FinancialRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a financial record",
               description = "Create a new income or expense record (Admin only). Supports idempotency via Idempotency-Key header.")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request) {
        RecordResponse record = recordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(record, "Financial record created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "List financial records",
               description = "Paginated, filterable list of financial records (Admin / Analyst)")
    public ResponseEntity<ApiResponse<PagedResponse<RecordResponse>>> getRecords(
            @Parameter(description = "Filter by type: INCOME or EXPENSE") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) String minAmount,
            @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) String maxAmount,
            @Parameter(description = "Search in description and notes") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        RecordFilterCriteria criteria = new RecordFilterCriteria();
        if (type != null) criteria.setType(com.zorvyn.finance.finance.domain.RecordType.valueOf(type.toUpperCase()));
        if (category != null) criteria.setCategory(com.zorvyn.finance.finance.domain.RecordCategory.valueOf(category.toUpperCase()));
        if (startDate != null) criteria.setStartDate(java.time.LocalDate.parse(startDate));
        if (endDate != null) criteria.setEndDate(java.time.LocalDate.parse(endDate));
        if (minAmount != null) criteria.setMinAmount(new java.math.BigDecimal(minAmount));
        if (maxAmount != null) criteria.setMaxAmount(new java.math.BigDecimal(maxAmount));
        if (search != null) criteria.setSearch(search);

        PagedResponse<RecordResponse> records = recordService.getRecords(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(records, "Records retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get a financial record", description = "Get record details by ID (Admin / Analyst)")
    public ResponseEntity<ApiResponse<RecordResponse>> getRecordById(@PathVariable UUID id) {
        RecordResponse record = recordService.getRecordById(id);
        return ResponseEntity.ok()
                .header("ETag", "\"" + record.getVersion() + "\"")
                .body(ApiResponse.success(record));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a financial record",
               description = "Partial update with optimistic locking (Admin only). Send current version in request body.")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecordRequest request) {
        RecordResponse record = recordService.updateRecord(id, request);
        return ResponseEntity.ok()
                .header("ETag", "\"" + record.getVersion() + "\"")
                .body(ApiResponse.success(record, "Financial record updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a financial record", description = "Soft-delete a record (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Financial record deleted successfully"));
    }
}
