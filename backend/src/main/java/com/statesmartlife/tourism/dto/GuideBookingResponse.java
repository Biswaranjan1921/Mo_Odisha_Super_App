package com.statesmartlife.tourism.dto;

import com.statesmartlife.tourism.enums.GuideBookingStatus;
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
public class GuideBookingResponse {
    private UUID id;
    private UUID guideId;
    private String guideFullName;
    private UUID touristId;
    private LocalDate bookingDate;
    private int durationHours;
    private BigDecimal totalFee;
    private GuideBookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
