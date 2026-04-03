package com.zorvyn.finance.finance.application;

import com.zorvyn.finance.finance.domain.FinancialRecord;
import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import com.zorvyn.finance.finance.dto.CreateRecordRequest;
import com.zorvyn.finance.finance.dto.RecordFilterCriteria;
import com.zorvyn.finance.finance.dto.RecordResponse;
import com.zorvyn.finance.finance.mapper.RecordMapper;
import com.zorvyn.finance.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.shared.audit.AuditEventService;
import com.zorvyn.finance.shared.dto.PagedResponse;
import com.zorvyn.finance.shared.exception.ResourceNotFoundException;
import com.zorvyn.finance.shared.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialRecordService Unit Tests")
class FinancialRecordServiceTest {

    @Mock private FinancialRecordRepository recordRepository;
    @Mock private RecordMapper recordMapper;
    @Mock private AuditEventService auditEventService;

    @InjectMocks
    private FinancialRecordService recordService;

    private UUID userId;
    private FinancialRecord sampleRecord;
    private RecordResponse sampleResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Set up security context
        UserPrincipal principal = new UserPrincipal(userId, "admin@zorvyn.com", null, "ADMIN", true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        sampleRecord = new FinancialRecord();
        sampleRecord.setId(UUID.randomUUID());
        sampleRecord.setUserId(userId);
        sampleRecord.setAmount(new BigDecimal("5000.00"));
        sampleRecord.setType(RecordType.INCOME);
        sampleRecord.setCategory(RecordCategory.SALARY);
        sampleRecord.setDescription("Monthly salary");
        sampleRecord.setTransactionDate(LocalDate.of(2026, 3, 15));

        sampleResponse = RecordResponse.builder()
                .id(sampleRecord.getId())
                .amount(sampleRecord.getAmount())
                .type(RecordType.INCOME)
                .category(RecordCategory.SALARY)
                .description("Monthly salary")
                .transactionDate(LocalDate.of(2026, 3, 15))
                .build();
    }

    @Test
    @DisplayName("Create: should create a financial record")
    void createRecord_shouldCreateAndReturnRecord() {
        CreateRecordRequest request = new CreateRecordRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setType(RecordType.INCOME);
        request.setCategory(RecordCategory.SALARY);
        request.setDescription("Monthly salary");
        request.setTransactionDate(LocalDate.of(2026, 3, 15));

        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(sampleRecord);
        when(recordMapper.toResponse(any(FinancialRecord.class))).thenReturn(sampleResponse);

        RecordResponse response = recordService.createRecord(request);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(response.getType()).isEqualTo(RecordType.INCOME);
        verify(recordRepository).save(any(FinancialRecord.class));
        verify(auditEventService).logAction(eq("RECORD_CREATED"), eq("FinancialRecord"), any(), isNull(), any());
    }

    @Test
    @DisplayName("GetById: should return record when it exists")
    void getRecordById_shouldReturnRecord() {
        UUID recordId = sampleRecord.getId();
        when(recordRepository.findActiveById(recordId)).thenReturn(Optional.of(sampleRecord));
        when(recordMapper.toResponse(sampleRecord)).thenReturn(sampleResponse);

        RecordResponse response = recordService.getRecordById(recordId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(recordId);
    }

    @Test
    @DisplayName("GetById: should throw ResourceNotFoundException when not found")
    void getRecordById_shouldThrowWhenNotFound() {
        UUID recordId = UUID.randomUUID();
        when(recordRepository.findActiveById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(recordId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("FinancialRecord");
    }

    @Test
    @DisplayName("GetRecords: should return paginated results with filters")
    @SuppressWarnings("unchecked")
    void getRecords_shouldReturnPagedResults() {
        RecordFilterCriteria criteria = new RecordFilterCriteria();
        criteria.setType(RecordType.INCOME);
        Pageable pageable = PageRequest.of(0, 20);

        Page<FinancialRecord> page = new PageImpl<>(List.of(sampleRecord), pageable, 1);
        when(recordRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(recordMapper.toResponse(sampleRecord)).thenReturn(sampleResponse);

        PagedResponse<RecordResponse> response = recordService.getRecords(criteria, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.isFirst()).isTrue();
    }

    @Test
    @DisplayName("Delete: should soft-delete record")
    void deleteRecord_shouldSoftDelete() {
        UUID recordId = sampleRecord.getId();
        when(recordRepository.findActiveById(recordId)).thenReturn(Optional.of(sampleRecord));
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(sampleRecord);
        when(recordMapper.toResponse(any(FinancialRecord.class))).thenReturn(sampleResponse);

        recordService.deleteRecord(recordId);

        assertThat(sampleRecord.isDeleted()).isTrue();
        verify(recordRepository).save(sampleRecord);
        verify(auditEventService).logAction(eq("RECORD_DELETED"), eq("FinancialRecord"), any(), any(), isNull());
    }
}
