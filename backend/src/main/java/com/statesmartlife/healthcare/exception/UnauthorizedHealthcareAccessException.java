package com.statesmartlife.healthcare.exception;

public class UnauthorizedHealthcareAccessException extends RuntimeException {
    public UnauthorizedHealthcareAccessException(String message) {
        super(message);
    }
}
