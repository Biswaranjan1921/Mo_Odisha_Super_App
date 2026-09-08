package com.statesmartlife.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.common.security.JwtProvider;
import com.statesmartlife.user.dto.UpdateUserProfileRequest;
import com.statesmartlife.user.entity.UserProfileEntity;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAll();
        userRepository.deleteAll();

        String uniquePhone = "+91" + (System.currentTimeMillis() % 10000000000L);
        String uniqueEmail = "user" + System.currentTimeMillis() + "@example.com";

        UserEntity user = UserEntity.builder()
                .phoneNumber(uniquePhone)
                .email(uniqueEmail)
                .passwordHash("hashedSecret")
                .role("CITIZEN")
                .active(true)
                .build();
        UserEntity savedUser = userRepository.save(user);
        userId = savedUser.getId();

        jwtToken = jwtProvider.createAccessToken(userId.toString(), savedUser.getEmail(), "CITIZEN");

        // Seed initial profile in test DB
        UserProfileEntity profile = UserProfileEntity.builder()
                .userId(userId)
                .fullName("Test Odisha Citizen")
                .dateOfBirth(LocalDate.now().minusYears(60))
                .homeAddress("Bhubaneswar")
                .build();
        userProfileRepository.save(profile);
    }

    @Test
    @DisplayName("GET /api/v1/users/me returns 200 OK and own profile for authenticated user")
    void testGetProfileMeAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.fullName").value("Test Odisha Citizen"))
                .andExpect(jsonPath("$.age").value(60))
                .andExpect(jsonPath("$.elderly").value(true))
                .andExpect(jsonPath("$.accessibility.largeText").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/users/me returns 401 Unauthorized for unauthenticated request")
    void testGetProfileMeUnauthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/users/me updates profile and returns updated response")
    void testUpdateProfileMeAuthenticated() throws Exception {
        LocalDate dob55 = LocalDate.now().minusYears(55);
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .fullName("Updated Odisha Citizen")
                .dateOfBirth(dob55)
                .homeAddress("Puri")
                .build();

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Odisha Citizen"))
                .andExpect(jsonPath("$.age").value(55))
                .andExpect(jsonPath("$.elderly").value(false))
                .andExpect(jsonPath("$.accessibility.largeText").value(false));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me rejects future DOB with 400 Bad Request")
    void testUpdateProfileMeFutureDOB() throws Exception {
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .fullName("Future Citizen")
                .dateOfBirth(LocalDate.now().plusDays(5))
                .build();

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
