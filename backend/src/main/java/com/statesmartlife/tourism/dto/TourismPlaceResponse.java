package com.statesmartlife.tourism.dto;

import com.statesmartlife.tourism.enums.TourismCategory;
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
public class TourismPlaceResponse {
    private UUID id;
    private String name;
    private String descriptionEn;
    private String descriptionLocal;
    private TourismCategory category;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int averageVisitDurationMinutes;
    private BigDecimal entryFee;
    private String imageUrl;
}
