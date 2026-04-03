package com.zorvyn.finance.iam.application;

import com.zorvyn.finance.iam.domain.Role;
import com.zorvyn.finance.iam.domain.User;
import com.zorvyn.finance.iam.domain.UserStatus;
import com.zorvyn.finance.iam.dto.UpdateUserRequest;
import com.zorvyn.finance.iam.dto.UserResponse;
import com.zorvyn.finance.iam.mapper.UserMapper;
import com.zorvyn.finance.iam.repository.RoleRepository;
import com.zorvyn.finance.iam.repository.UserRepository;
import com.zorvyn.finance.shared.audit.AuditEventService;
import com.zorvyn.finance.shared.dto.PagedResponse;
import com.zorvyn.finance.shared.exception.BusinessRuleException;
import com.zorvyn.finance.shared.exception.DuplicateResourceException;
import com.zorvyn.finance.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * User management service for ADMIN operations.
 * Handles CRUD, role assignment, and status management.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuditEventService auditEventService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserMapper userMapper,
                       AuditEventService auditEventService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.auditEventService = auditEventService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAllActive(pageable);
        return PagedResponse.of(
                page.getContent().stream().map(userMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        UserResponse oldState = userMapper.toResponse(user);

        // Apply partial updates (only non-null fields)
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User", "email", newEmail);
            }
            user.setEmail(newEmail);
        }
        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRoleName()));
            user.setRole(role);
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User saved = userRepository.save(user);
        log.info("User updated: {}", saved.getEmail());

        auditEventService.logAction("USER_UPDATED", "User", saved.getId(), oldState, userMapper.toResponse(saved));

        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse updateUserStatus(UUID id, UserStatus status) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        User saved = userRepository.save(user);

        log.info("User {} status changed: {} → {}", saved.getEmail(), oldStatus, status);
        auditEventService.logAction("USER_STATUS_CHANGED", "User", saved.getId(), oldStatus, status);

        return userMapper.toResponse(saved);
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.softDelete();
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("User soft-deleted: {}", user.getEmail());
        auditEventService.logAction("USER_DELETED", "User", user.getId(), null, null);
    }
}
