package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.PropertyRateSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRateSettingsRepository extends JpaRepository<PropertyRateSettings, Long> {
    Optional<PropertyRateSettings> findByPropertyId(Long propertyId);

}
