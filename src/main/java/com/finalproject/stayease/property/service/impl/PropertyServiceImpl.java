package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
@Slf4j
public class PropertyServiceImpl implements PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyCategoryService propertyCategoryService;


  @Override
  public List<Property> findAll() {
    List<Property> propertyList = propertyRepository.findAll();
    if (propertyList.isEmpty()) {
      throw new DataNotFoundException("No property found");
    }
    return propertyList;
  }

  @Override
  public List<Property> findAllByTenant(Users tenant) {
    isTenant(tenant);
    List<Property> tenantsProperties = propertyRepository.findByTenantAndDeletedAtIsNull(tenant);
    if (tenantsProperties.isEmpty()) {
      throw new DataNotFoundException("You have no properties yet");
    }
    return tenantsProperties;
  }

  @Override
  public Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO) {
    isTenant(tenant);
    return toPropertyEntity(tenant, requestDTO);
  }

  @Override
  public Property updateProperty(Users tenant, Long propertyId, UpdatePropertyRequestDTO requestDTO) {
    Property existingProperty = checkIfValid(tenant, propertyId);
    return update(existingProperty, requestDTO);
  }

  @Override
  public void deleteProperty(Users tenant, Long propertyId) {
    Property existingProperty = checkIfValid(tenant, propertyId);
    existingProperty.setDeletedAt(Instant.now());
    propertyRepository.save(existingProperty);
  }

  @Override
  public Optional<Property> findPropertyById(Long id) {
    return propertyRepository.findByIdAndDeletedAtIsNull(id);
  }

  private void isTenant(Users tenant) {
    if (tenant.getUserType() != UserType.TENANT) {
      throw new InvalidRequestException("Only Tenants can create properties");
    }
  }

  private Property toPropertyEntity(Users tenant, CreatePropertyRequestDTO requestDTO) {
    Point point = toGeographyPoint(requestDTO.getLongitude(), requestDTO.getLatitude());

    PropertyCategory category = getCategoryById(requestDTO.getCategoryId());

    Property property = new Property();
    property.setTenant(tenant);
    property.setCategory(category);
    property.setName(requestDTO.getName());
    property.setDescription(requestDTO.getDescription());
    property.setImagesUrl(requestDTO.getImages());
    property.setAddress(requestDTO.getAddress());
    property.setCity(requestDTO.getCity());
    property.setCountry(requestDTO.getCountry());
    property.setLocation(point);
    property.setLongitude(requestDTO.getLongitude());
    property.setLatitude(requestDTO.getLatitude());
    propertyRepository.save(property);
    return property;
  }

  private PropertyCategory getCategoryById(Long propertyCategoryId) {
    return
        propertyCategoryService.findCategoryByIdAndNotDeleted(propertyCategoryId)
            .orElseThrow(() -> new DataNotFoundException(
                "Category not found, please enter a valid category ID"));
  }

  private Point toGeographyPoint(double longitude, double latitude) {
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

    Optional<Property> checkProp = propertyRepository.findByLocationAndDeletedAtIsNull(point);
    if (checkProp.isPresent()) {
      // TODO : make ex DuplicatePropertyException
      throw new DuplicateEntryException("Property at this location already exist.");
    }
    return point;
  }

  private Property checkIfValid(Users tenant, Long categoryId) {
    // TODO : make ex PropertyNotFoundException
    Property existingProperty = propertyRepository.findByIdAndDeletedAtIsNull(categoryId).orElseThrow(
        () -> new InvalidRequestException("Property with this ID does not exist or is deleted")
    );
    isTenant(tenant);
    Users propertyOwner = existingProperty.getTenant();
    if (tenant != propertyOwner) {
      // TODO : make ex UnauthorizedOperationException
      throw new BadCredentialsException("You are not the owner of this property");
    }
    return existingProperty;
  }

  private Property update(Property existingProperty, UpdatePropertyRequestDTO requestDTO) {
    PropertyCategory updatedCategory = getUpdatedCategory(existingProperty, requestDTO);
    Optional.ofNullable(updatedCategory).ifPresent(existingProperty::setCategory);
    Optional.ofNullable(requestDTO.getName()).ifPresent(existingProperty::setName);
    Optional.ofNullable(requestDTO.getDescription()).ifPresent(existingProperty::setDescription);
    Optional.ofNullable(requestDTO.getImages()).ifPresent(existingProperty::setImagesUrl);
    propertyRepository.save(existingProperty);
    return existingProperty;
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

  // Region - quarantine

//  private Point getUpdatedPoint(Property existingProperty, UpdatePropertyRequestDTO requestDTO) {
//    Double existingLongitude = existingProperty.getLongitude();
//    Double existingLatitude = existingProperty.getLatitude();
//    Double requestLongitude = requestDTO.getLongitude();
//    Double requestLatitude = requestDTO.getLatitude();
//    if (requestLongitude != null && requestLatitude != null) {
//      if (existingLongitude.equals(requestLongitude) && existingLatitude.equals(requestLongitude)) {
//        return toGeographyPoint(requestLongitude, requestLatitude);
//      }
//    }
//    return null;
//  }
}
