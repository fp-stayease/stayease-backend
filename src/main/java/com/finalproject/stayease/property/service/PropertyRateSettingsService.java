package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.PropertyRateSettings;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import java.time.LocalDate;

public interface PropertyRateSettingsService {

    PropertyRateSettings getOrCreatePropertyRateSettings(Long propertyId);

    PropertyRateSettings updatePropertyRateSettings(Long propertyId, SetPropertyRateSettingsDTO request);

    void applySettingsForProperty(Long propertyId, SetPropertyRateSettingsDTO request);

    void applySettingsForProperty(Long propertyId, SetPropertyRateSettingsDTO request, LocalDate startDate, LocalDate endDate);

    void deactivateAutoRates(Long propertyId);
}
