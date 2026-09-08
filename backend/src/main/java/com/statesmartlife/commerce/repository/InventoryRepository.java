package com.statesmartlife.commerce.repository;

import com.statesmartlife.commerce.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByProductId(UUID productId);

    @Modifying
    @Query("UPDATE InventoryEntity i SET i.stockQuantity = i.stockQuantity - :quantity WHERE i.productId = :productId AND i.stockQuantity >= :quantity")
    int deductStockAtomically(@Param("productId") UUID productId, @Param("quantity") int quantity);
}
