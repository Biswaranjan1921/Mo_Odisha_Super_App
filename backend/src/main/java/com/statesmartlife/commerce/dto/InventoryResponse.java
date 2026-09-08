package com.statesmartlife.commerce.dto;

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
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private int stockQuantity;
    private int lowStockThreshold;
    private Instant updatedAt;
}
