package com.statesmartlife.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a domain business rule is violated.
 */
public class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BusinessRuleException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "BUSINESS_RULE_VIOLATION";
    }

    public BusinessRuleException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
