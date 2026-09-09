package com.statesmartlife.emergency.exception;

public class ActiveEmergencyAlreadyExistsException extends RuntimeException {
    public ActiveEmergencyAlreadyExistsException(String message) {
        super(message);
    }
}
