package com.statesmartlife.governance.repository;

import com.statesmartlife.governance.entity.OdishaDistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OdishaDistrictRepository extends JpaRepository<OdishaDistrictEntity, UUID> {
    List<OdishaDistrictEntity> findByIsActiveTrueOrderByDistrictNameAsc();
    Optional<OdishaDistrictEntity> findByDistrictCode(String districtCode);
}
