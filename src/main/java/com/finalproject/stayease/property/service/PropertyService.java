package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;

public interface PropertyService {
    Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO);

    Optional<Property> findPropertyById(Long id);
}
