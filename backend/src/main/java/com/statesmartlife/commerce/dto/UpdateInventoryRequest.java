package com.statesmartlife.commerce.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryRequest {

    @Min(value = 0, message = "Stock quantity must be non-negative")
    private int stockQuantity;
}
