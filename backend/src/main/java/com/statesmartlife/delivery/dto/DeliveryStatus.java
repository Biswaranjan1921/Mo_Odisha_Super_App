package com.statesmartlife.delivery.dto;

public enum DeliveryStatus {
    ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    FAILED;

    public boolean canTransitionTo(DeliveryStatus next) {
        if (next == null) return false;
        if (this == ASSIGNED) return next == PICKED_UP || next == FAILED;
        if (this == PICKED_UP) return next == IN_TRANSIT || next == FAILED;
        if (this == IN_TRANSIT) return next == DELIVERED || next == FAILED;
        return false; // DELIVERED and FAILED are terminal states
    }
}
