package com.statesmartlife.user.mapper;

import com.statesmartlife.user.dto.AccessibilityDefaultsDto;
import com.statesmartlife.user.dto.UserProfileResponse;
import com.statesmartlife.user.entity.UserProfileEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        int age = calculateAge(entity.getDateOfBirth());
        boolean isElderly = age >= 60;
        AccessibilityDefaultsDto accessibility = AccessibilityDefaultsDto.forElderlyMode(isElderly);

        return UserProfileResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fullName(entity.getFullName())
                .dateOfBirth(entity.getDateOfBirth())
                .age(age)
                .isElderly(isElderly)
                .avatarUrl(entity.getAvatarUrl())
                .emergencyContact(entity.getEmergencyContact())
                .homeAddress(entity.getHomeAddress())
                .accessibility(accessibility)
                .build();
    }

    public int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
