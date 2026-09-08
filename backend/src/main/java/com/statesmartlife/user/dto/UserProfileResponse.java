package com.statesmartlife.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private int age;
    private boolean isElderly;
    private String avatarUrl;
    private String emergencyContact;
    private String homeAddress;
    private AccessibilityDefaultsDto accessibility;
}
