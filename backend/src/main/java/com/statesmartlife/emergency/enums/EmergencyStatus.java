package com.statesmartlife.emergency.enums;

public enum EmergencyStatus {
    REPORTED,
    DISPATCHED,
    EN_ROUTE,
    ON_SCENE,
    RESOLVED,
    CANCELLED;

    public boolean canTransitionTo(EmergencyStatus target) {
        return switch (this) {
            case REPORTED -> target == DISPATCHED || target == CANCELLED;
            case DISPATCHED -> target == EN_ROUTE || target == CANCELLED;
            case EN_ROUTE -> target == ON_SCENE || target == CANCELLED;
            case ON_SCENE -> target == RESOLVED;
            case RESOLVED, CANCELLED -> false;
        };
    }
}
