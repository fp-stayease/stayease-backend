package com.finalproject.stayease.property.service.helpers;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.exceptions.properties.DuplicatePropertyException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class PropertyServiceHelper {

  private final PropertyRepository propertyRepository;
  private final PropertyCategoryService propertyCategoryService;
  public void isTenant(Users tenant) {
    if (tenant.getUserType() != UserType.TENANT) {
      throw new UnauthorizedOperationsException("Only Tenants can create properties");
    }
  }

  public Property toPropertyEntity(Users tenant, CreatePropertyRequestDTO requestDTO) {
    Point point = toGeographyPoint(requestDTO.getLongitude(), requestDTO.getLatitude());

    PropertyCategory category = getCategoryById(requestDTO.getCategoryId());

    Property property = new Property();
    property.setTenant(tenant);
    property.setCategory(category);
    property.setName(requestDTO.getName());
    property.setDescription(requestDTO.getDescription());
    property.setImageUrl(requestDTO.getImageUrl());
    property.setAddress(requestDTO.getAddress());
    property.setCity(requestDTO.getCity());
    property.setCountry(requestDTO.getCountry());
    property.setLocation(point);
    property.setLongitude(requestDTO.getLongitude());
    property.setLatitude(requestDTO.getLatitude());
    propertyRepository.save(property);
    return property;
  }

  public Property checkIfValid(Users tenant, Long categoryId) {
    Property existingProperty = propertyRepository.findByIdAndDeletedAtIsNull(categoryId).orElseThrow(
        () -> new PropertyNotFoundException("Property with this ID does not exist or is deleted")
    );
    isTenant(tenant);
    Users propertyOwner = existingProperty.getTenant();
    if (tenant != propertyOwner) {
      throw new UnauthorizedOperationsException("You are not the owner of this property");
    }
    return existingProperty;
  }

  public Property update(Property existingProperty, UpdatePropertyRequestDTO requestDTO) {
    PropertyCategory updatedCategory = getUpdatedCategory(existingProperty, requestDTO);
    Optional.ofNullable(updatedCategory).ifPresent(existingProperty::setCategory);
    Optional.ofNullable(requestDTO.getName()).ifPresent(existingProperty::setName);
    Optional.ofNullable(requestDTO.getDescription()).ifPresent(existingProperty::setDescription);
    Optional.ofNullable(requestDTO.getImageUrl()).ifPresent(existingProperty::setImageUrl);
    propertyRepository.save(existingProperty);
    return existingProperty;
  }

  public void validateDate(LocalDate date) {
    if (date.isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Date is out of valid range: " + date);
    }
  }

  public void validateDate(LocalDate startDate, LocalDate endDate) {
    validateDate(startDate);
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
  }

  public PropertyCategory getCategoryById(Long propertyCategoryId) {
    return
        propertyCategoryService.findCategoryByIdAndNotDeleted(propertyCategoryId)
            .orElseThrow(() -> new CategoryNotFoundException(
                "Category not found, please enter a valid category ID"));
  }

  private Point toGeographyPoint(double longitude, double latitude) {
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

    Optional<Property> checkProp = propertyRepository.findByLocationAndDeletedAtIsNull(point);
    if (checkProp.isPresent()) {
      throw new DuplicatePropertyException("Property at this location already exist.");
    }
    return point;
  }


  private PropertyCategory getUpdatedCategory(Property existingProperty, UpdatePropertyRequestDTO requestDTO) {
    if (requestDTO.getCategoryId() != null) {
      PropertyCategory existingCategory = existingProperty.getCategory();
      PropertyCategory requestedCategory = getCategoryById(requestDTO.getCategoryId());
      if (existingCategory != requestedCategory) {
        return requestedCategory;
      }
    }
    return null;
  }
}
