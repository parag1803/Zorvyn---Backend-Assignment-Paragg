package com.zorvyn.finance.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated.
 * Examples: inactive user trying to login, insufficient permissions, invalid state transition.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
