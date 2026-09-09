package com.statesmartlife.tourism.service;

import com.statesmartlife.auth.repository.UserRepository;
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
import com.statesmartlife.tourism.entity.GuideBookingEntity;
import com.statesmartlife.tourism.entity.GuideDestinationEntity;
import com.statesmartlife.tourism.entity.GuideGalleryEntity;
import com.statesmartlife.tourism.entity.TourismGuideApplicationEntity;
import com.statesmartlife.tourism.entity.TourismGuideEntity;
import com.statesmartlife.tourism.entity.TourismPlaceEntity;
import com.statesmartlife.tourism.enums.GuideApplicationStatus;
import com.statesmartlife.tourism.enums.GuideBookingStatus;
import com.statesmartlife.tourism.enums.TourismCategory;
import com.statesmartlife.tourism.exception.GuideApplicationAlreadyExistsException;
import com.statesmartlife.tourism.exception.GuideApplicationNotFoundException;
import com.statesmartlife.tourism.exception.GuideBookingSlotUnavailableException;
import com.statesmartlife.tourism.exception.TourGuideNotFoundException;
import com.statesmartlife.tourism.exception.TourismPlaceNotFoundException;
import com.statesmartlife.tourism.exception.UnauthorizedTourismAccessException;
import com.statesmartlife.tourism.repository.GuideBookingRepository;
import com.statesmartlife.tourism.repository.GuideDestinationRepository;
import com.statesmartlife.tourism.repository.GuideGalleryRepository;
import com.statesmartlife.tourism.repository.TourismGuideApplicationRepository;
import com.statesmartlife.tourism.repository.TourismGuideRepository;
import com.statesmartlife.tourism.repository.TourismPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourismServiceImpl implements TourismService {

    private final TourismPlaceRepository placeRepository;
    private final TourismGuideApplicationRepository applicationRepository;
    private final TourismGuideRepository guideRepository;
    private final GuideGalleryRepository galleryRepository;
    private final GuideDestinationRepository destinationRepository;
    private final GuideBookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TourismGuideApplicationResponse applyForGuide(UUID userId, ApplyGuideRequest request) {
        applicationRepository.findByUserId(userId).ifPresent(app -> {
            if (app.getStatus() == GuideApplicationStatus.PENDING || app.getStatus() == GuideApplicationStatus.APPROVED) {
                throw new GuideApplicationAlreadyExistsException("You already have an active or approved guide application");
            }
        });

        String userFullName = userRepository.findById(userId)
                .map(u -> u.getEmail()) // fallback or default name
                .orElse("Tour Guide Candidate");

        TourismGuideApplicationEntity application = TourismGuideApplicationEntity.builder()
                .userId(userId)
                .fullName(userFullName)
                .licenseNumber(request.getLicenseNumber())
                .idProofUrl(request.getIdProofUrl())
                .experienceYears(request.getExperienceYears())
                .languagesSpoken(request.getLanguagesSpoken())
                .status(GuideApplicationStatus.PENDING)
                .build();

        TourismGuideApplicationEntity saved = applicationRepository.save(application);
        return mapToApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourismGuideApplicationResponse> getPendingGuideApplications() {
        return applicationRepository.findByStatusOrderByCreatedAtDesc(GuideApplicationStatus.PENDING)
                .stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TourismGuideApplicationResponse auditGuideApplication(UUID applicationId, AuditGuideApplicationRequest request, UUID adminId) {
        TourismGuideApplicationEntity app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new GuideApplicationNotFoundException("Guide application not found: " + applicationId));

        if (request.isApprove()) {
            app.setStatus(GuideApplicationStatus.APPROVED);
            // Create or update certified guide record
            TourismGuideEntity guide = guideRepository.findByUserId(app.getUserId())
                    .orElseGet(() -> TourismGuideEntity.builder()
                            .userId(app.getUserId())
                            .fullName(app.getFullName())
                            .languagesSpoken(app.getLanguagesSpoken())
                            .experienceYears(app.getExperienceYears())
                            .hourlyRate(new BigDecimal("500.00"))
                            .isVerified(true)
                            .isAvailable(true)
                            .build());

            guide.setVerified(true);
            guideRepository.save(guide);
        } else {
            app.setStatus(GuideApplicationStatus.REJECTED);
            app.setRejectionReason(request.getRejectionReason());
        }

        TourismGuideApplicationEntity saved = applicationRepository.save(app);
        return mapToApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TourismGuideResponse getMyGuideProfile(UUID userId) {
        TourismGuideEntity guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified tour guide profile not found for user: " + userId));
        return mapToGuideResponse(guide);
    }

    @Override
    @Transactional
    public TourismGuideResponse updateGuideProfile(UUID userId, UpdateGuideProfileRequest request) {
        TourismGuideEntity guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified tour guide profile not found for user: " + userId));

        if (request.getProfessionalHeadline() != null) guide.setProfessionalHeadline(request.getProfessionalHeadline());
        if (request.getAboutMe() != null) guide.setAboutMe(request.getAboutMe());
        if (request.getProfileImageUrl() != null) guide.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getCoverImageUrl() != null) guide.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getLanguagesSpoken() != null) guide.setLanguagesSpoken(request.getLanguagesSpoken());
        if (request.getExperienceYears() >= 0) guide.setExperienceYears(request.getExperienceYears());
        if (request.getSpecialization() != null) guide.setSpecialization(request.getSpecialization());
        if (request.getHourlyRate() != null) guide.setHourlyRate(request.getHourlyRate());
        if (request.getIsAvailable() != null) guide.setAvailable(request.getIsAvailable());

        TourismGuideEntity saved = guideRepository.save(guide);
        return mapToGuideResponse(saved);
    }

    @Override
    @Transactional
    public PublicGuideProfileResponse.GalleryImageResponse addGalleryImage(UUID userId, AddGalleryImageRequest request) {
        TourismGuideEntity guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified tour guide profile not found for user: " + userId));

        GuideGalleryEntity gallery = GuideGalleryEntity.builder()
                .guideId(guide.getId())
                .imageUrl(request.getImageUrl())
                .caption(request.getCaption())
                .displayOrder(request.getDisplayOrder())
                .isCover(request.isCover())
                .build();

        GuideGalleryEntity saved = galleryRepository.save(gallery);
        return PublicGuideProfileResponse.GalleryImageResponse.builder()
                .id(saved.getId())
                .imageUrl(saved.getImageUrl())
                .caption(saved.getCaption())
                .displayOrder(saved.getDisplayOrder())
                .isCover(saved.isCover())
                .build();
    }

    @Override
    @Transactional
    public void deleteGalleryImage(UUID userId, UUID galleryId) {
        TourismGuideEntity guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified tour guide profile not found for user: " + userId));

        GuideGalleryEntity gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new TourismPlaceNotFoundException("Gallery image not found: " + galleryId));

        if (!gallery.getGuideId().equals(guide.getId())) {
            throw new UnauthorizedTourismAccessException("You are not authorized to delete this gallery photo");
        }

        galleryRepository.delete(gallery);
    }

    @Override
    @Transactional
    public TourismGuideResponse updateGuideDestinations(UUID userId, List<UUID> placeIds) {
        TourismGuideEntity guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified tour guide profile not found for user: " + userId));

        destinationRepository.deleteByGuideId(guide.getId());

        for (UUID placeId : placeIds) {
            if (placeRepository.existsById(placeId)) {
                destinationRepository.save(GuideDestinationEntity.builder()
                        .guideId(guide.getId())
                        .tourismPlaceId(placeId)
                        .build());
            }
        }

        return mapToGuideResponse(guide);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicGuideProfileResponse getPublicGuideProfile(UUID guideId) {
        TourismGuideEntity guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified tour guide not found: " + guideId));

        List<PublicGuideProfileResponse.GalleryImageResponse> gallery = galleryRepository.findByGuideIdOrderByDisplayOrderAsc(guide.getId())
                .stream()
                .map(g -> PublicGuideProfileResponse.GalleryImageResponse.builder()
                        .id(g.getId())
                        .imageUrl(g.getImageUrl())
                        .caption(g.getCaption())
                        .displayOrder(g.getDisplayOrder())
                        .isCover(g.isCover())
                        .build())
                .collect(Collectors.toList());

        List<UUID> placeIds = destinationRepository.findByGuideId(guide.getId())
                .stream()
                .map(GuideDestinationEntity::getTourismPlaceId)
                .collect(Collectors.toList());

        List<TourismPlaceResponse> destinations = placeRepository.findAllById(placeIds)
                .stream()
                .map(this::mapToPlaceResponse)
                .collect(Collectors.toList());

        return PublicGuideProfileResponse.builder()
                .id(guide.getId())
                .userId(guide.getUserId())
                .fullName(guide.getFullName())
                .professionalHeadline(guide.getProfessionalHeadline())
                .aboutMe(guide.getAboutMe())
                .profileImageUrl(guide.getProfileImageUrl())
                .coverImageUrl(guide.getCoverImageUrl())
                .languagesSpoken(guide.getLanguagesSpoken())
                .experienceYears(guide.getExperienceYears())
                .specialization(guide.getSpecialization())
                .hourlyRate(guide.getHourlyRate())
                .isVerified(guide.isVerified())
                .isAvailable(guide.isAvailable())
                .gallery(gallery)
                .destinations(destinations)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourismGuideResponse> getVerifiedGuides() {
        return guideRepository.findByIsVerifiedTrueAndIsAvailableTrue()
                .stream()
                .map(this::mapToGuideResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourismPlaceResponse> getAllPlaces(TourismCategory category, String search) {
        List<TourismPlaceEntity> places;
        if (category != null) {
            places = placeRepository.findByCategory(category);
        } else if (search != null && !search.isBlank()) {
            places = placeRepository.findByNameContainingIgnoreCase(search);
        } else {
            places = placeRepository.findAll();
        }
        return places.stream().map(this::mapToPlaceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TourismPlaceResponse getPlaceById(UUID id) {
        TourismPlaceEntity place = placeRepository.findById(id)
                .orElseThrow(() -> new TourismPlaceNotFoundException("Tourism place not found: " + id));
        return mapToPlaceResponse(place);
    }

    @Override
    @Transactional
    public GuideBookingResponse bookGuide(UUID touristId, BookGuideRequest request) {
        TourismGuideEntity guide = guideRepository.findById(request.getGuideId())
                .orElseThrow(() -> new TourGuideNotFoundException("Tour guide not found: " + request.getGuideId()));

        if (!guide.isAvailable() || !guide.isVerified()) {
            throw new GuideBookingSlotUnavailableException("Tour guide is currently unavailable or unverified");
        }

        boolean doubleBooked = bookingRepository.existsByGuideIdAndBookingDateAndStatusIn(
                guide.getId(),
                request.getBookingDate(),
                List.of(GuideBookingStatus.BOOKED, GuideBookingStatus.CONFIRMED)
        );

        if (doubleBooked) {
            throw new GuideBookingSlotUnavailableException("Tour guide is already booked on " + request.getBookingDate());
        }

        BigDecimal totalFee = guide.getHourlyRate().multiply(BigDecimal.valueOf(request.getDurationHours()));

        GuideBookingEntity booking = GuideBookingEntity.builder()
                .guideId(guide.getId())
                .touristId(touristId)
                .bookingDate(request.getBookingDate())
                .durationHours(request.getDurationHours())
                .totalFee(totalFee)
                .status(GuideBookingStatus.BOOKED)
                .build();

        GuideBookingEntity saved = bookingRepository.save(booking);
        return mapToBookingResponse(saved, guide.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideBookingResponse> getMyGuideBookings(UUID touristId) {
        return bookingRepository.findByTouristIdOrderByBookingDateDesc(touristId)
                .stream()
                .map(b -> {
                    String guideName = guideRepository.findById(b.getGuideId()).map(TourismGuideEntity::getFullName).orElse("Certified Guide");
                    return mapToBookingResponse(b, guideName);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideBookingResponse> getGuideBookingsForGuide(UUID userId) {
        TourismGuideEntity guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new TourGuideNotFoundException("Certified guide profile not found for user: " + userId));

        return bookingRepository.findByGuideIdOrderByBookingDateDesc(guide.getId())
                .stream()
                .map(b -> mapToBookingResponse(b, guide.getFullName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GuideBookingResponse updateGuideBookingStatus(UUID bookingId, GuideBookingStatus status, UUID userId) {
        GuideBookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new TourismPlaceNotFoundException("Guide booking not found: " + bookingId));

        booking.setStatus(status);
        GuideBookingEntity saved = bookingRepository.save(booking);
        String guideName = guideRepository.findById(saved.getGuideId()).map(TourismGuideEntity::getFullName).orElse("Certified Guide");
        return mapToBookingResponse(saved, guideName);
    }

    private TourismGuideApplicationResponse mapToApplicationResponse(TourismGuideApplicationEntity app) {
        return TourismGuideApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUserId())
                .fullName(app.getFullName())
                .licenseNumber(app.getLicenseNumber())
                .idProofUrl(app.getIdProofUrl())
                .experienceYears(app.getExperienceYears())
                .languagesSpoken(app.getLanguagesSpoken())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    private TourismGuideResponse mapToGuideResponse(TourismGuideEntity guide) {
        return TourismGuideResponse.builder()
                .id(guide.getId())
                .userId(guide.getUserId())
                .fullName(guide.getFullName())
                .professionalHeadline(guide.getProfessionalHeadline())
                .aboutMe(guide.getAboutMe())
                .profileImageUrl(guide.getProfileImageUrl())
                .coverImageUrl(guide.getCoverImageUrl())
                .languagesSpoken(guide.getLanguagesSpoken())
                .experienceYears(guide.getExperienceYears())
                .specialization(guide.getSpecialization())
                .hourlyRate(guide.getHourlyRate())
                .isVerified(guide.isVerified())
                .isAvailable(guide.isAvailable())
                .build();
    }

    private TourismPlaceResponse mapToPlaceResponse(TourismPlaceEntity place) {
        return TourismPlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .descriptionEn(place.getDescriptionEn())
                .descriptionLocal(place.getDescriptionLocal())
                .category(place.getCategory())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .averageVisitDurationMinutes(place.getAverageVisitDurationMinutes())
                .entryFee(place.getEntryFee())
                .imageUrl(place.getImageUrl())
                .build();
    }

    private GuideBookingResponse mapToBookingResponse(GuideBookingEntity booking, String guideName) {
        return GuideBookingResponse.builder()
                .id(booking.getId())
                .guideId(booking.getGuideId())
                .guideFullName(guideName)
                .touristId(booking.getTouristId())
                .bookingDate(booking.getBookingDate())
                .durationHours(booking.getDurationHours())
                .totalFee(booking.getTotalFee())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
