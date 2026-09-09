package com.statesmartlife.healthcare.dto;

import com.statesmartlife.healthcare.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private UUID doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String hospitalName;
    private UUID patientId;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private String symptomsDescription;
    private BigDecimal consultationFee;
    private String prescriptionNotes;
    private String prescriptionUrl;
    private Instant completedAt;
    private Instant createdAt;
}
