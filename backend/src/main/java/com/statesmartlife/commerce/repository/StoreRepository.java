package com.statesmartlife.commerce.repository;

import com.statesmartlife.commerce.dto.CommerceCategory;
import com.statesmartlife.commerce.entity.StoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {
    Page<StoreEntity> findByActiveTrue(Pageable pageable);
    Page<StoreEntity> findByCategoryAndActiveTrue(CommerceCategory category, Pageable pageable);
}
