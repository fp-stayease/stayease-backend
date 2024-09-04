package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.CreatePropertyRequestDTO;
import com.finalproject.stayease.users.entity.Users;

public interface PropertyService {
    Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO);
}
