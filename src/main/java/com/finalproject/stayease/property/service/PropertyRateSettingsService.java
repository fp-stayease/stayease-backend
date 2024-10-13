package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import java.time.LocalDate;

public interface PropertyRateSettingsService {

    PropertyRateSetting getOrCreatePropertyRateSettings(Long propertyId);

    PropertyRateSetting updatePropertyRateSettings(Long propertyId, SetPropertyRateSettingsDTO request);

    void applySettingForProperty(PropertyRateSetting setting);

    void applySettingForProperty(PropertyRateSetting setting, LocalDate startDate, LocalDate endDate);

    void deactivateAutoRates(Long propertyId);
}
