package com.statesmartlife.user.service;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.user.dto.UpdateUserProfileRequest;
import com.statesmartlife.user.dto.UserProfileResponse;
import com.statesmartlife.user.entity.UserProfileEntity;
import com.statesmartlife.user.mapper.UserProfileMapper;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserProfileRepository userProfileRepository;
    private UserProfileMapper userProfileMapper;
    private UserService userService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userProfileRepository = mock(UserProfileRepository.class);
        userProfileMapper = new UserProfileMapper();
        userService = new UserService(userProfileRepository, userProfileMapper);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Age 59 years (364 days) must result in isElderly = false")
    void testAge59IsElderlyFalse() {
        LocalDate dobAge59 = LocalDate.now().minusYears(60).plusDays(1);
        UserProfileEntity profile = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Test Citizen")
                .dateOfBirth(dobAge59)
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileResponse response = userService.getProfile(userId);

        assertNotNull(response);
        assertEquals(59, response.getAge());
        assertFalse(response.isElderly());
        assertFalse(response.getAccessibility().isLargeText());
        assertFalse(response.getAccessibility().isHighContrast());
    }

    @Test
    @DisplayName("Age exactly 60 years today must result in isElderly = true")
    void testAgeExactly60IsElderlyTrue() {
        LocalDate dobAge60 = LocalDate.now().minusYears(60);
        UserProfileEntity profile = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Senior Citizen")
                .dateOfBirth(dobAge60)
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileResponse response = userService.getProfile(userId);

        assertNotNull(response);
        assertEquals(60, response.getAge());
        assertTrue(response.isElderly());
        assertTrue(response.getAccessibility().isLargeText());
        assertTrue(response.getAccessibility().isHighContrast());
        assertTrue(response.getAccessibility().isVoiceAssistance());
    }

    @Test
    @DisplayName("Age 65 years (61+) must result in isElderly = true")
    void testAge61PlusIsElderlyTrue() {
        LocalDate dobAge65 = LocalDate.now().minusYears(65);
        UserProfileEntity profile = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Elderly Citizen")
                .dateOfBirth(dobAge65)
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileResponse response = userService.getProfile(userId);

        assertNotNull(response);
        assertEquals(65, response.getAge());
        assertTrue(response.isElderly());
        assertTrue(response.getAccessibility().isLargeText());
    }

    @Test
    @DisplayName("Future date of birth must throw BusinessRuleException (400 Bad Request)")
    void testFutureDOBRejected() {
        LocalDate futureDob = LocalDate.now().plusDays(1);
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .fullName("Future Person")
                .dateOfBirth(futureDob)
                .build();

        assertThrows(BusinessRuleException.class, () -> userService.updateProfile(userId, request));
    }

    @Test
    @DisplayName("Updating profile persists changes and recalculates DOB age engine")
    void testUpdateProfileSuccess() {
        LocalDate dob62 = LocalDate.now().minusYears(62);
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .fullName("Updated Citizen")
                .dateOfBirth(dob62)
                .avatarUrl("https://example.com/avatar.png")
                .emergencyContact("+919999988888")
                .homeAddress("Cuttack, Odisha")
                .build();

        UserProfileEntity existing = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Old Name")
                .dateOfBirth(LocalDate.now().minusYears(30))
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response = userService.updateProfile(userId, request);

        assertNotNull(response);
        assertEquals("Updated Citizen", response.getFullName());
        assertEquals(62, response.getAge());
        assertTrue(response.isElderly());
        assertEquals("+919999988888", response.getEmergencyContact());
    }
}
