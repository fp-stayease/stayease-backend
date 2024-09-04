package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
@Slf4j
public class PropertyServiceImpl implements PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyCategoryService propertyCategoryService;


  @Override
  public Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO) {
    isTenant(tenant);
    return toPropertyEntity(tenant, requestDTO);
  }

  @Override
  public Optional<Property> findPropertyById(Long id) {
    return propertyRepository.findById(id);
  }

  private void isTenant(Users tenant) {
    if (tenant.getUserType() != UserType.TENANT) {
      throw new InvalidRequestException("Only Tenants can create properties");
    }
  }

  private Property toPropertyEntity(Users tenant, CreatePropertyRequestDTO requestDTO) {
    Point point = toGeographyPoint(requestDTO.getLongitude(), requestDTO.getLatitude());

    PropertyCategory category =
        propertyCategoryService.findCategoryByIdAndNotDeleted(requestDTO.getCategoryId()).orElseThrow(() -> new DataNotFoundException(
            "Category not found, please enter a valid category ID"));

    Property property = new Property();
    property.setTenant(tenant);
    property.setCategory(category);
    property.setName(requestDTO.getName());
    property.setDescription(requestDTO.getDescription());
    property.setPicture(requestDTO.getPicture());
    property.setAddress(requestDTO.getAddress());
    property.setCity(requestDTO.getCity());
    property.setCountry(requestDTO.getCountry());
    property.setLocation(point);
    property.setLongitude(requestDTO.getLongitude());
    property.setLatitude(requestDTO.getLatitude());
    propertyRepository.save(property);
    return property;
  }

  private Point toGeographyPoint(double longitude, double latitude) {
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

    Optional<Property> checkProp = propertyRepository.findByLocation(point);
    if (checkProp.isPresent()) {
      throw new DuplicateEntryException("Property at this location already exist.");
    }
    return point;
  }
}
