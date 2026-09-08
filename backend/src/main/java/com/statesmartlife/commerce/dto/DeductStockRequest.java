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
public class DeductStockRequest {

    @Min(value = 1, message = "Quantity to deduct must be at least 1")
    private int quantity;
}
