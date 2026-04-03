package com.zorvyn.finance.finance.application;

import com.zorvyn.finance.finance.domain.FinancialRecord;
import com.zorvyn.finance.finance.dto.*;
import com.zorvyn.finance.finance.mapper.RecordMapper;
import com.zorvyn.finance.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.finance.specification.RecordSpecification;
import com.zorvyn.finance.shared.audit.AuditEventService;
import com.zorvyn.finance.shared.dto.PagedResponse;
import com.zorvyn.finance.shared.exception.ResourceNotFoundException;
import com.zorvyn.finance.shared.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Financial record service handling CRUD operations with audit logging,
 * cache invalidation, and specification-based filtering.
 */
@Service
public class FinancialRecordService {

    private static final Logger log = LoggerFactory.getLogger(FinancialRecordService.class);

    private final FinancialRecordRepository recordRepository;
    private final RecordMapper recordMapper;
    private final AuditEventService auditEventService;

    public FinancialRecordService(FinancialRecordRepository recordRepository,
                                   RecordMapper recordMapper,
                                   AuditEventService auditEventService) {
        this.recordRepository = recordRepository;
        this.recordMapper = recordMapper;
        this.auditEventService = auditEventService;
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categoryBreakdown", "monthlyTrends", "recentActivity"}, allEntries = true)
    public RecordResponse createRecord(CreateRecordRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("No authenticated user"));

        FinancialRecord record = new FinancialRecord();
        record.setUserId(currentUserId);
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDescription(request.getDescription());
        record.setNotes(request.getNotes());
        record.setTransactionDate(request.getTransactionDate());

        FinancialRecord saved = recordRepository.save(record);
        log.info("Financial record created: {} {} {}", saved.getId(), saved.getType(), saved.getAmount());

        auditEventService.logAction("RECORD_CREATED", "FinancialRecord",
                saved.getId(), null, recordMapper.toResponse(saved));

        return recordMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RecordResponse> getRecords(RecordFilterCriteria criteria, Pageable pageable) {
        Specification<FinancialRecord> spec = RecordSpecification.fromCriteria(criteria);
        Page<FinancialRecord> page = recordRepository.findAll(spec, pageable);

        return PagedResponse.of(
                page.getContent().stream().map(recordMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public RecordResponse getRecordById(UUID id) {
        FinancialRecord record = recordRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", "id", id));
        return recordMapper.toResponse(record);
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categoryBreakdown", "monthlyTrends", "recentActivity"}, allEntries = true)
    public RecordResponse updateRecord(UUID id, UpdateRecordRequest request) {
        FinancialRecord record = recordRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", "id", id));

        RecordResponse oldState = recordMapper.toResponse(record);

        // Set the version for optimistic locking
        record.setVersion(request.getVersion());

        // Apply partial updates
        if (request.getAmount() != null) record.setAmount(request.getAmount());
        if (request.getType() != null) record.setType(request.getType());
        if (request.getCategory() != null) record.setCategory(request.getCategory());
        if (request.getDescription() != null) record.setDescription(request.getDescription());
        if (request.getNotes() != null) record.setNotes(request.getNotes());
        if (request.getTransactionDate() != null) record.setTransactionDate(request.getTransactionDate());

        FinancialRecord saved = recordRepository.save(record);
        log.info("Financial record updated: {}", saved.getId());

        auditEventService.logAction("RECORD_UPDATED", "FinancialRecord",
                saved.getId(), oldState, recordMapper.toResponse(saved));

        return recordMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categoryBreakdown", "monthlyTrends", "recentActivity"}, allEntries = true)
    public void deleteRecord(UUID id) {
        FinancialRecord record = recordRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", "id", id));

        record.softDelete();
        recordRepository.save(record);

        log.info("Financial record soft-deleted: {}", id);
        auditEventService.logAction("RECORD_DELETED", "FinancialRecord",
                record.getId(), recordMapper.toResponse(record), null);
    }
}
