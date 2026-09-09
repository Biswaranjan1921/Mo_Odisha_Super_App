package com.statesmartlife.tourism.entity;

import com.statesmartlife.tourism.enums.TourismCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tbl_tourism_places")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourismPlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description_en", nullable = false, columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_local", columnDefinition = "TEXT")
    private String descriptionLocal;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private TourismCategory category;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    @Builder.Default
    private BigDecimal latitude = new BigDecimal("20.2961000");

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    @Builder.Default
    private BigDecimal longitude = new BigDecimal("85.8245000");

    @Column(name = "average_visit_duration_minutes", nullable = false)
    @Builder.Default
    private int averageVisitDurationMinutes = 60;

    @Column(name = "entry_fee", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal entryFee = BigDecimal.ZERO;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
