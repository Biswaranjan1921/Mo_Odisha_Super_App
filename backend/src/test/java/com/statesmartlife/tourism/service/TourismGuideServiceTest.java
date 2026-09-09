package com.statesmartlife.tourism.service;

import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.tourism.dto.AddGalleryImageRequest;
import com.statesmartlife.tourism.dto.ApplyGuideRequest;
import com.statesmartlife.tourism.dto.AuditGuideApplicationRequest;
import com.statesmartlife.tourism.dto.BookGuideRequest;
import com.statesmartlife.tourism.dto.GuideBookingResponse;
import com.statesmartlife.tourism.dto.PublicGuideProfileResponse;
import com.statesmartlife.tourism.dto.TourismGuideApplicationResponse;
import com.statesmartlife.tourism.dto.TourismGuideResponse;
import com.statesmartlife.tourism.dto.UpdateGuideProfileRequest;
import com.statesmartlife.tourism.entity.GuideBookingEntity;
import com.statesmartlife.tourism.entity.GuideGalleryEntity;
import com.statesmartlife.tourism.entity.TourismGuideApplicationEntity;
import com.statesmartlife.tourism.entity.TourismGuideEntity;
import com.statesmartlife.tourism.enums.GuideApplicationStatus;
import com.statesmartlife.tourism.enums.GuideBookingStatus;
import com.statesmartlife.tourism.exception.GuideApplicationAlreadyExistsException;
import com.statesmartlife.tourism.exception.GuideBookingSlotUnavailableException;
import com.statesmartlife.tourism.repository.GuideBookingRepository;
import com.statesmartlife.tourism.repository.GuideDestinationRepository;
import com.statesmartlife.tourism.repository.GuideGalleryRepository;
import com.statesmartlife.tourism.repository.TourismGuideApplicationRepository;
import com.statesmartlife.tourism.repository.TourismGuideRepository;
import com.statesmartlife.tourism.repository.TourismPlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TourismGuideServiceTest {

    private TourismPlaceRepository placeRepository;
    private TourismGuideApplicationRepository applicationRepository;
    private TourismGuideRepository guideRepository;
    private GuideGalleryRepository galleryRepository;
    private GuideDestinationRepository destinationRepository;
    private GuideBookingRepository bookingRepository;
    private UserRepository userRepository;
    private TourismServiceImpl tourismService;

    private UUID userId;
    private UUID adminId;
    private UUID guideId;
    private UUID touristId;

    @BeforeEach
    void setUp() {
        placeRepository = mock(TourismPlaceRepository.class);
        applicationRepository = mock(TourismGuideApplicationRepository.class);
        guideRepository = mock(TourismGuideRepository.class);
        galleryRepository = mock(GuideGalleryRepository.class);
        destinationRepository = mock(GuideDestinationRepository.class);
        bookingRepository = mock(GuideBookingRepository.class);
        userRepository = mock(UserRepository.class);

        tourismService = new TourismServiceImpl(
                placeRepository,
                applicationRepository,
                guideRepository,
                galleryRepository,
                destinationRepository,
                bookingRepository,
                userRepository
        );

        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        guideId = UUID.randomUUID();
        touristId = UUID.randomUUID();
    }

    @Test
    @DisplayName("applyForGuide - Submits application successfully")
    void applyForGuide_Success() {
        when(applicationRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserEntity.builder().email("guide@test.com").build()));
        when(applicationRepository.save(any(TourismGuideApplicationEntity.class))).thenAnswer(i -> {
            TourismGuideApplicationEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ApplyGuideRequest request = ApplyGuideRequest.builder()
                .licenseNumber("LIC-12345")
                .experienceYears(5)
                .languagesSpoken("Odia, English")
                .build();

        TourismGuideApplicationResponse response = tourismService.applyForGuide(userId, request);

        assertNotNull(response);
        assertEquals(GuideApplicationStatus.PENDING, response.getStatus());
        assertEquals("LIC-12345", response.getLicenseNumber());
    }

    @Test
    @DisplayName("applyForGuide - Throws GuideApplicationAlreadyExistsException if pending application exists")
    void applyForGuide_Duplicate_ThrowsException() {
        TourismGuideApplicationEntity app = TourismGuideApplicationEntity.builder()
                .status(GuideApplicationStatus.PENDING)
                .build();
        when(applicationRepository.findByUserId(userId)).thenReturn(Optional.of(app));

        ApplyGuideRequest request = ApplyGuideRequest.builder().licenseNumber("LIC-12345").build();

        assertThrows(GuideApplicationAlreadyExistsException.class, () -> tourismService.applyForGuide(userId, request));
    }

    @Test
    @DisplayName("auditGuideApplication - Approval initializes certified guide profile")
    void auditGuideApplication_Approved_InitializesGuide() {
        UUID appId = UUID.randomUUID();
        TourismGuideApplicationEntity app = TourismGuideApplicationEntity.builder()
                .id(appId)
                .userId(userId)
                .fullName("Ramesh Behera")
                .languagesSpoken("Odia, English")
                .experienceYears(8)
                .status(GuideApplicationStatus.PENDING)
                .build();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(TourismGuideApplicationEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(guideRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(guideRepository.save(any(TourismGuideEntity.class))).thenAnswer(i -> {
            TourismGuideEntity guide = i.getArgument(0);
            guide.setId(guideId);
            return guide;
        });

        AuditGuideApplicationRequest auditRequest = AuditGuideApplicationRequest.builder().approve(true).build();

        TourismGuideApplicationResponse response = tourismService.auditGuideApplication(appId, auditRequest, adminId);

        assertEquals(GuideApplicationStatus.APPROVED, response.getStatus());
        verify(guideRepository).save(argThat(g -> g.isVerified() && g.getFullName().equals("Ramesh Behera")));
    }

    @Test
    @DisplayName("updateGuideProfile - Updates guide headline, about me, and hourly rate")
    void updateGuideProfile_Success() {
        TourismGuideEntity guide = TourismGuideEntity.builder()
                .id(guideId)
                .userId(userId)
                .fullName("Ramesh Behera")
                .hourlyRate(new BigDecimal("500.00"))
                .isVerified(true)
                .build();

        when(guideRepository.findByUserId(userId)).thenReturn(Optional.of(guide));
        when(guideRepository.save(any(TourismGuideEntity.class))).thenAnswer(i -> i.getArgument(0));

        UpdateGuideProfileRequest request = UpdateGuideProfileRequest.builder()
                .professionalHeadline("Expert Heritage Tour Guide")
                .aboutMe("Certified guide with 8+ years experience in Konark Temple walks")
                .hourlyRate(new BigDecimal("1000.00"))
                .build();

        TourismGuideResponse response = tourismService.updateGuideProfile(userId, request);

        assertEquals("Expert Heritage Tour Guide", response.getProfessionalHeadline());
        assertEquals(new BigDecimal("1000.00"), response.getHourlyRate());
    }

    @Test
    @DisplayName("addGalleryImage - Saves gallery photo for guide")
    void addGalleryImage_Success() {
        TourismGuideEntity guide = TourismGuideEntity.builder().id(guideId).userId(userId).build();
        when(guideRepository.findByUserId(userId)).thenReturn(Optional.of(guide));
        when(galleryRepository.save(any(GuideGalleryEntity.class))).thenAnswer(i -> {
            GuideGalleryEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AddGalleryImageRequest request = AddGalleryImageRequest.builder()
                .imageUrl("https://images.unsplash.com/photo-1590050752117-238cb0fb12b1")
                .caption("Konark Sun Temple Walk")
                .isCover(true)
                .build();

        PublicGuideProfileResponse.GalleryImageResponse response = tourismService.addGalleryImage(userId, request);

        assertNotNull(response);
        assertEquals("Konark Sun Temple Walk", response.getCaption());
        assertTrue(response.isCover());
    }

    @Test
    @DisplayName("getPublicGuideProfile - Returns full profile with gallery and destinations")
    void getPublicGuideProfile_Success() {
        TourismGuideEntity guide = TourismGuideEntity.builder()
                .id(guideId)
                .userId(userId)
                .fullName("Ramesh Behera")
                .professionalHeadline("Certified Heritage Guide")
                .hourlyRate(new BigDecimal("1000.00"))
                .isVerified(true)
                .isAvailable(true)
                .build();

        when(guideRepository.findById(guideId)).thenReturn(Optional.of(guide));
        when(galleryRepository.findByGuideIdOrderByDisplayOrderAsc(guideId)).thenReturn(Collections.emptyList());
        when(destinationRepository.findByGuideId(guideId)).thenReturn(Collections.emptyList());

        PublicGuideProfileResponse response = tourismService.getPublicGuideProfile(guideId);

        assertNotNull(response);
        assertEquals("Ramesh Behera", response.getFullName());
        assertTrue(response.isVerified());
    }

    @Test
    @DisplayName("bookGuide - Calculates total fee and creates booking")
    void bookGuide_Success() {
        TourismGuideEntity guide = TourismGuideEntity.builder()
                .id(guideId)
                .fullName("Ramesh Behera")
                .hourlyRate(new BigDecimal("1000.00"))
                .isVerified(true)
                .isAvailable(true)
                .build();

        when(guideRepository.findById(guideId)).thenReturn(Optional.of(guide));
        when(bookingRepository.existsByGuideIdAndBookingDateAndStatusIn(any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(GuideBookingEntity.class))).thenAnswer(i -> {
            GuideBookingEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        BookGuideRequest request = BookGuideRequest.builder()
                .guideId(guideId)
                .bookingDate(LocalDate.now().plusDays(2))
                .durationHours(4)
                .build();

        GuideBookingResponse response = tourismService.bookGuide(touristId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("4000.00"), response.getTotalFee()); // 1000 * 4 hours
        assertEquals(GuideBookingStatus.BOOKED, response.getStatus());
    }

    @Test
    @DisplayName("bookGuide - Throws GuideBookingSlotUnavailableException if double-booked on same date")
    void bookGuide_DoubleBooking_ThrowsException() {
        TourismGuideEntity guide = TourismGuideEntity.builder().id(guideId).isVerified(true).isAvailable(true).build();
        when(guideRepository.findById(guideId)).thenReturn(Optional.of(guide));
        when(bookingRepository.existsByGuideIdAndBookingDateAndStatusIn(any(), any(), any())).thenReturn(true);

        BookGuideRequest request = BookGuideRequest.builder()
                .guideId(guideId)
                .bookingDate(LocalDate.now().plusDays(2))
                .durationHours(3)
                .build();

        assertThrows(GuideBookingSlotUnavailableException.class, () -> tourismService.bookGuide(touristId, request));
    }
}
