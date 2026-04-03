package com.zorvyn.finance.iam.domain;

/**
 * User account status lifecycle.
 *
 * PENDING → ACTIVE (after verification, or immediately on creation)
 * ACTIVE → INACTIVE (admin deactivates)
 * ACTIVE → SUSPENDED (policy violation)
 * INACTIVE → ACTIVE (admin reactivates)
 * SUSPENDED → ACTIVE (admin lifts suspension)
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING
}
