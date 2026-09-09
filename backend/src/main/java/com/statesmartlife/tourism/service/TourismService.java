package com.statesmartlife.tourism.service;

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

import java.util.List;
import java.util.UUID;

public interface TourismService {

    TourismGuideApplicationResponse applyForGuide(UUID userId, ApplyGuideRequest request);

    List<TourismGuideApplicationResponse> getPendingGuideApplications();

    TourismGuideApplicationResponse auditGuideApplication(UUID applicationId, AuditGuideApplicationRequest request, UUID adminId);

    TourismGuideResponse getMyGuideProfile(UUID userId);

    TourismGuideResponse updateGuideProfile(UUID userId, UpdateGuideProfileRequest request);

    PublicGuideProfileResponse.GalleryImageResponse addGalleryImage(UUID userId, AddGalleryImageRequest request);

    void deleteGalleryImage(UUID userId, UUID galleryId);

    TourismGuideResponse updateGuideDestinations(UUID userId, List<UUID> placeIds);

    PublicGuideProfileResponse getPublicGuideProfile(UUID guideId);

    List<TourismGuideResponse> getVerifiedGuides();

    List<TourismPlaceResponse> getAllPlaces(TourismCategory category, String search);

    TourismPlaceResponse getPlaceById(UUID id);

    GuideBookingResponse bookGuide(UUID touristId, BookGuideRequest request);

    List<GuideBookingResponse> getMyGuideBookings(UUID touristId);

    List<GuideBookingResponse> getGuideBookingsForGuide(UUID userId);

    GuideBookingResponse updateGuideBookingStatus(UUID bookingId, GuideBookingStatus status, UUID userId);
}
