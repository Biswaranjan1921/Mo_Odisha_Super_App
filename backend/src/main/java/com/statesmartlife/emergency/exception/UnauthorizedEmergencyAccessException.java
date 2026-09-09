package com.statesmartlife.emergency.exception;

public class UnauthorizedEmergencyAccessException extends RuntimeException {
    public UnauthorizedEmergencyAccessException(String message) {
        super(message);
    }
}
