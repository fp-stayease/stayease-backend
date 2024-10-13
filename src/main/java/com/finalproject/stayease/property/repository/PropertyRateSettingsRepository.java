package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.PropertyRateSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRateSettingsRepository extends JpaRepository<PropertyRateSetting, Long> {
    Optional<PropertyRateSetting> findByPropertyId(Long propertyId);

}
