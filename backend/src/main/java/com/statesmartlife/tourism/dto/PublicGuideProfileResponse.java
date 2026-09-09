package com.statesmartlife.tourism.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicGuideProfileResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String professionalHeadline;
    private String aboutMe;
    private String profileImageUrl;
    private String coverImageUrl;
    private String languagesSpoken;
    private int experienceYears;
    private String specialization;
    private BigDecimal hourlyRate;
    private boolean isVerified;
    private boolean isAvailable;
    private List<GalleryImageResponse> gallery;
    private List<TourismPlaceResponse> destinations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GalleryImageResponse {
        private UUID id;
        private String imageUrl;
        private String caption;
        private int displayOrder;
        private boolean isCover;
    }
}
