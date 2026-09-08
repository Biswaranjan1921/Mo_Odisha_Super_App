package com.statesmartlife.auth.repository;

import com.statesmartlife.auth.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, UUID> {
    Optional<EmailVerificationEntity> findByVerificationTokenHashAndUsedFalse(String tokenHash);
    void deleteByUserId(UUID userId);
}
