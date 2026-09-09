package com.statesmartlife.events.dto;

import com.statesmartlife.events.enums.EventBookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBookingResponse {
    private UUID id;
    private UUID venueId;
    private String venueName;
    private UUID plannerId;
    private LocalDate eventDate;
    private BigDecimal totalBudget;
    private String vendorRequirements;
    private EventBookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
