package com.statesmartlife.healthcare.exception;

public class AppointmentSlotUnavailableException extends RuntimeException {
    public AppointmentSlotUnavailableException(String message) {
        super(message);
    }
}
