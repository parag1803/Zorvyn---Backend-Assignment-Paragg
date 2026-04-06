package com.zorvyn.finance.finance.api;

import com.zorvyn.finance.finance.application.FinancialRecordService;
import com.zorvyn.finance.finance.dto.*;
import com.zorvyn.finance.shared.dto.ErrorResponse;
import com.zorvyn.finance.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/records")
@Tag(
    name = "Financial Records",
    description = "CRUD operations for financial transactions (income & expenses).\n\n" +
                  "**RBAC:** `ADMIN` → create, update, delete. `ANALYST` + `ADMIN` → read.\n\n" +
                  "**Idempotency:** Add `Idempotency-Key: <uuid>` header to `POST /records` to safely retry " +
                  "without creating duplicates.\n\n" +
                  "**Optimistic Locking:** Include the current `version` field in update requests to prevent " +
                  "lost-update conflicts."
)
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    public FinancialRecordController(FinancialRecordService recordService) {
        this.recordService = recordService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /records
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create a financial record",
        description = "Create a new income or expense record. **Admin only.**\n\n" +
                      "Supports idempotency — include `Idempotency-Key: <uuid>` header to safely retry " +
                      "without creating duplicates.",
        parameters = {
            @Parameter(
                name = "Idempotency-Key",
                in = ParameterIn.HEADER,
                required = false,
                description = "A UUID v4 that uniquely identifies this request. If a request with the same key " +
                              "was processed within the last 24 hours, the original response is returned.",
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Record created successfully",
            headers = @Header(name = "Location", description = "URL of the new record")),
        @ApiResponse(responseCode = "400", description = "Validation failed — check amount, type, category, date",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Idempotency conflict — duplicate request key",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request) {
        RecordResponse record = recordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.zorvyn.finance.shared.dto.ApiResponse.created(record, "Financial record created successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /records
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary = "List financial records",
        description = "Paginated, filterable list of financial records. **ANALYST + ADMIN only.**\n\n" +
                      "All filters are optional and combinable. Results are sorted by `transactionDate` DESC by default."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ANALYST or ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<PagedResponse<RecordResponse>>> getRecords(
            @Parameter(description = "Filter by type", example = "INCOME",
                schema = @Schema(allowableValues = {"INCOME", "EXPENSE"}))
            @RequestParam(required = false) String type,
            @Parameter(description = "Filter by category", example = "SALARY",
                schema = @Schema(allowableValues = {"SALARY","FREELANCE","INVESTMENT","BUSINESS","RENTAL",
                    "FOOD","HOUSING","TRANSPORT","UTILITIES","HEALTHCARE","ENTERTAINMENT","EDUCATION",
                    "SHOPPING","INSURANCE","TAXES","DONATION","OTHER"}))
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter records on or after this date (yyyy-MM-dd)", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter records on or before this date (yyyy-MM-dd)", example = "2024-12-31")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Filter records with amount >= this value", example = "100.00")
            @RequestParam(required = false) String minAmount,
            @Parameter(description = "Filter records with amount <= this value", example = "5000.00")
            @RequestParam(required = false) String maxAmount,
            @Parameter(description = "Full-text search in description and notes", example = "salary")
            @RequestParam(required = false) String search,
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
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(records, "Records retrieved successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /records/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary = "Get a record by ID",
        description = "Fetch a single financial record by its UUID. Returns an `ETag` header with the current version " +
                      "for use in optimistic locking on updates. **ANALYST + ADMIN only.**"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record found",
            headers = @Header(name = "ETag", description = "Current version of the record for optimistic locking")),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Record not found or soft-deleted",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<RecordResponse>> getRecordById(@PathVariable UUID id) {
        RecordResponse record = recordService.getRecordById(id);
        return ResponseEntity.ok()
                .header("ETag", "\"" + record.getVersion() + "\"")
                .body(com.zorvyn.finance.shared.dto.ApiResponse.success(record));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /records/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update a financial record",
        description = "Partial update — only non-null fields are applied. **Admin only.**\n\n" +
                      "Uses **optimistic locking**: include the current `version` (from the GET response or ETag) " +
                      "in the request body. A 409 is returned if the record was concurrently modified."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record updated successfully",
            headers = @Header(name = "ETag", description = "New version after update")),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Record not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Optimistic locking conflict — record was modified concurrently",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<RecordResponse>> updateRecord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecordRequest request) {
        RecordResponse record = recordService.updateRecord(id, request);
        return ResponseEntity.ok()
                .header("ETag", "\"" + record.getVersion() + "\"")
                .body(com.zorvyn.finance.shared.dto.ApiResponse.success(record, "Financial record updated successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /records/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete a financial record",
        description = "Soft-deletes a record (sets `deletedAt` timestamp). The record is excluded from all " +
                      "queries but retained in the database for audit purposes. **Admin only.**"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Record deleted successfully — no content returned"),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Record not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
