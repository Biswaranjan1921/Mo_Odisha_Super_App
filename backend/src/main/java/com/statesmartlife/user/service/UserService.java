package com.statesmartlife.user.service;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.user.dto.UpdateUserProfileRequest;
import com.statesmartlife.user.dto.UserProfileResponse;
import com.statesmartlife.user.entity.UserProfileEntity;
import com.statesmartlife.user.mapper.UserProfileMapper;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public UserService(
            UserProfileRepository userProfileRepository,
            UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessRuleException(
                        "USER_PROFILE_NOT_FOUND", "User profile not found", HttpStatus.NOT_FOUND));
        return userProfileMapper.toResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        if (request.getDateOfBirth() != null && request.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "INVALID_DOB", "Date of birth cannot be in the future", HttpStatus.BAD_REQUEST);
        }

        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfileEntity.builder().userId(userId).build());

        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setHomeAddress(request.getHomeAddress());

        UserProfileEntity saved = userProfileRepository.save(profile);
        return userProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public com.statesmartlife.user.dto.DashboardSummaryResponse getDashboardSummary(UUID userId) {
        UserProfileResponse profileResponse = getProfile(userId);
        return com.statesmartlife.user.dto.DashboardSummaryResponse.builder()
                .profile(profileResponse)
                .activeOrdersCount(0)
                .activeAppointmentsCount(0)
                .activeTransitPassesCount(0)
                .systemNotice("State Smart Life — Mo Odisha Super App Services Active")
                .build();
    }
}
