package com.statesmartlife.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {

    @NotBlank(message = "Store name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private CommerceCategory category;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;

    private Double latitude;
    private Double longitude;
}
