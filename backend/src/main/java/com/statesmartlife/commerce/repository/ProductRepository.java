package com.statesmartlife.commerce.repository;

import com.statesmartlife.commerce.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findByStoreId(UUID storeId, Pageable pageable);
    Page<ProductEntity> findByIsMedicine(boolean isMedicine, Pageable pageable);
}
