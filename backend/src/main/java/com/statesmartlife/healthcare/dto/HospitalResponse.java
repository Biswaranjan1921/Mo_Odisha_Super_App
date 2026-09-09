package com.statesmartlife.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse {

    private UUID id;
    private String name;
    private String address;
    private String phoneNumber;
    private boolean hasEmergencyService;
    private Instant createdAt;
}
