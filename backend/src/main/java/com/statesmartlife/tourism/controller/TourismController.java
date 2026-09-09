package com.statesmartlife.tourism.controller;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.tourism.dto.AddGalleryImageRequest;
import com.statesmartlife.tourism.dto.ApplyGuideRequest;
import com.statesmartlife.tourism.dto.AuditGuideApplicationRequest;
import com.statesmartlife.tourism.dto.BookGuideRequest;
import com.statesmartlife.tourism.dto.GuideBookingResponse;
import com.statesmartlife.tourism.dto.PublicGuideProfileResponse;
import com.statesmartlife.tourism.dto.TourismGuideApplicationResponse;
import com.statesmartlife.tourism.dto.TourismGuideResponse;
import com.statesmartlife.tourism.dto.TourismPlaceResponse;
import com.statesmartlife.tourism.dto.UpdateGuideProfileRequest;
import com.statesmartlife.tourism.enums.GuideBookingStatus;
import com.statesmartlife.tourism.enums.TourismCategory;
import com.statesmartlife.tourism.service.TourismService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TourismService tourismService;

    @GetMapping("/places")
    public ResponseEntity<List<TourismPlaceResponse>> getAllPlaces(
            @RequestParam(required = false) TourismCategory category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(tourismService.getAllPlaces(category, search));
    }

    @GetMapping("/places/{id}")
    public ResponseEntity<TourismPlaceResponse> getPlaceById(@PathVariable UUID id) {
        return ResponseEntity.ok(tourismService.getPlaceById(id));
    }

    @PostMapping("/guides/apply")
    public ResponseEntity<TourismGuideApplicationResponse> applyForGuide(
            @Valid @RequestBody ApplyGuideRequest request) {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(tourismService.applyForGuide(userId, request));
    }

    @GetMapping("/applications/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TourismGuideApplicationResponse>> getPendingGuideApplications() {
        return ResponseEntity.ok(tourismService.getPendingGuideApplications());
    }

    @PutMapping("/applications/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TourismGuideApplicationResponse> auditGuideApplication(
            @PathVariable UUID id,
            @RequestBody AuditGuideApplicationRequest request) {
        UUID adminId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.auditGuideApplication(id, request, adminId));
    }

    @GetMapping("/guides/me")
    public ResponseEntity<TourismGuideResponse> getMyGuideProfile() {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.getMyGuideProfile(userId));
    }

    @PutMapping("/guides/me")
    public ResponseEntity<TourismGuideResponse> updateGuideProfile(
            @Valid @RequestBody UpdateGuideProfileRequest request) {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.updateGuideProfile(userId, request));
    }

    @PostMapping("/guides/me/gallery")
    public ResponseEntity<PublicGuideProfileResponse.GalleryImageResponse> addGalleryImage(
            @Valid @RequestBody AddGalleryImageRequest request) {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(tourismService.addGalleryImage(userId, request));
    }

    @DeleteMapping("/guides/me/gallery/{galleryId}")
    public ResponseEntity<Void> deleteGalleryImage(@PathVariable UUID galleryId) {
        UUID userId = extractAuthenticatedUserId();
        tourismService.deleteGalleryImage(userId, galleryId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/guides/me/destinations")
    public ResponseEntity<TourismGuideResponse> updateGuideDestinations(
            @RequestBody List<UUID> placeIds) {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.updateGuideDestinations(userId, placeIds));
    }

    @GetMapping("/guides/{id}/public")
    public ResponseEntity<PublicGuideProfileResponse> getPublicGuideProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(tourismService.getPublicGuideProfile(id));
    }

    @GetMapping("/guides/verified")
    public ResponseEntity<List<TourismGuideResponse>> getVerifiedGuides() {
        return ResponseEntity.ok(tourismService.getVerifiedGuides());
    }

    @PostMapping("/guides/book")
    public ResponseEntity<GuideBookingResponse> bookGuide(
            @Valid @RequestBody BookGuideRequest request) {
        UUID touristId = extractAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(tourismService.bookGuide(touristId, request));
    }

    @GetMapping("/bookings/me")
    public ResponseEntity<List<GuideBookingResponse>> getMyGuideBookings() {
        UUID touristId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.getMyGuideBookings(touristId));
    }

    @GetMapping("/bookings/guide")
    public ResponseEntity<List<GuideBookingResponse>> getGuideBookingsForGuide() {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.getGuideBookingsForGuide(userId));
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<GuideBookingResponse> updateGuideBookingStatus(
            @PathVariable UUID id,
            @RequestParam GuideBookingStatus status) {
        UUID userId = extractAuthenticatedUserId();
        return ResponseEntity.ok(tourismService.updateGuideBookingStatus(id, status, userId));
    }

    private UUID extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            throw new BusinessRuleException("UNAUTHORIZED", "User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("INVALID_USER_ID", "Invalid user security principal", HttpStatus.UNAUTHORIZED);
        }
    }
}
