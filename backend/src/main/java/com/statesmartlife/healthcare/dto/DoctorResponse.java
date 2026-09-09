package com.statesmartlife.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private UUID id;
    private UUID hospitalId;
    private String hospitalName;
    private UUID userId;
    private String name;
    private String specialization;
    private String availabilitySchedule;
    private String licenseNumber;
    private BigDecimal consultationFee;
    private boolean isAvailable;
    private boolean isVerified;
    private Instant createdAt;
}
