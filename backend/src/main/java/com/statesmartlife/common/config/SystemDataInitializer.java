package com.statesmartlife.common.config;

import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.user.entity.UserProfileEntity;
import com.statesmartlife.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Seed default active ADMIN user if missing
        String adminEmail = "admin@smartlife.odisha.gov.in";
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Seeding default active ADMIN user: {}", adminEmail);
            UserEntity admin = UserEntity.builder()
                    .phoneNumber("9900000001")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("AdminPassword123!"))
                    .role("ADMIN")
                    .active(true)
                    .build();
            UserEntity savedAdmin = userRepository.save(admin);

            userProfileRepository.save(UserProfileEntity.builder()
                    .userId(savedAdmin.getId())
                    .fullName("State System Administrator")
                    .dateOfBirth(LocalDate.of(1985, 1, 1))
                    .build());
        }

        // Seed default active CUSTOMER user if missing
        String citizenEmail = "citizen@smartlife.odisha.gov.in";
        if (!userRepository.existsByEmail(citizenEmail)) {
            log.info("Seeding default active CUSTOMER user: {}", citizenEmail);
            UserEntity citizen = UserEntity.builder()
                    .phoneNumber("9900000002")
                    .email(citizenEmail)
                    .passwordHash(passwordEncoder.encode("CitizenPassword123!"))
                    .role("CUSTOMER")
                    .active(true)
                    .build();
            UserEntity savedCitizen = userRepository.save(citizen);

            userProfileRepository.save(UserProfileEntity.builder()
                    .userId(savedCitizen.getId())
                    .fullName("Odisha Smart Citizen")
                    .dateOfBirth(LocalDate.of(1995, 5, 15))
                    .build());
        }
    }
}
