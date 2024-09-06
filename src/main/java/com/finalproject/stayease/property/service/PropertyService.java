package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.util.List;
import java.util.Optional;

public interface PropertyService {
    List<Property> findAll();
    List<Property> findAllByTenant(Users tenant);
    Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO);
    Property updateProperty(Users tenant, Long propertyId, UpdatePropertyRequestDTO requestDTO);
    void deleteProperty(Users tenant, Long propertyId);

    Optional<Property> findPropertyById(Long id);
}
