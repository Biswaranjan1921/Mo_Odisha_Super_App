package com.statesmartlife.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private CommerceCategory category;
    private String address;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean active;
    private Double latitude;
    private Double longitude;
}
