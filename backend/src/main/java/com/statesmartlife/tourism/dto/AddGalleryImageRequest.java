package com.statesmartlife.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGalleryImageRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String caption;
    private int displayOrder;
    private boolean isCover;
}
