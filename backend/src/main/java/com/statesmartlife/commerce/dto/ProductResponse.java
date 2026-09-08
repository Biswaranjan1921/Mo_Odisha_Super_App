package com.statesmartlife.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private String imageUrl;
    private boolean isMedicine;
    private boolean requiresPrescription;
    private int stockQuantity;
    private boolean inStock;
}
