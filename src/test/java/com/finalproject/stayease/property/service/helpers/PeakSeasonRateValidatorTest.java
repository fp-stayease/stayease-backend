package com.finalproject.stayease.property.service.helpers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.ConflictingRateException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PeakSeasonRateValidatorTest {

  @Mock
  private PeakSeasonRateRepository peakSeasonRateRepository;

  @Mock
  private PropertyService propertyService;

  @InjectMocks
  private PeakSeasonRateValidator validator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testValidateDate_Valid() {
    LocalDate validDate = LocalDate.now().plusDays(1);
    assertDoesNotThrow(() -> validator.validateDate(validDate));
  }

  @Test
  void testValidateDate_Invalid() {
    LocalDate invalidDate = LocalDate.now().minusDays(1);
    assertThrows(InvalidDateException.class, () -> validator.validateDate(invalidDate));
  }

  @Test
  void testValidateDateRange_Valid() {
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = LocalDate.now().plusDays(7);
    assertDoesNotThrow(() -> validator.validateDateRange(startDate, endDate));
  }

  @Test
  void testValidateDateRange_Invalid() {
    LocalDate startDate = LocalDate.now().plusDays(7);
    LocalDate endDate = LocalDate.now().plusDays(1);
    assertThrows(InvalidDateException.class, () -> validator.validateDateRange(startDate, endDate));
  }

  @Test
  void testValidateRateDateRange_NoConflict() {
    Long propertyId = 1L;
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = LocalDate.now().plusDays(7);

    when(peakSeasonRateRepository.existsConflictingRate(propertyId, startDate, endDate)).thenReturn(false);

    assertDoesNotThrow(() -> validator.validateRateDateRange(propertyId, startDate, endDate));
    verify(peakSeasonRateRepository, times(1)).existsConflictingRate(propertyId, startDate, endDate);
  }

  @Test
  void testValidateRateDateRange_Conflict() {
    Long propertyId = 1L;
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = LocalDate.now().plusDays(7);

    when(peakSeasonRateRepository.existsConflictingRate(propertyId, startDate, endDate)).thenReturn(true);

    assertThrows(ConflictingRateException.class, () -> validator.validateRateDateRange(propertyId, startDate, endDate));
    verify(peakSeasonRateRepository, times(1)).existsConflictingRate(propertyId, startDate, endDate);
  }

  @Test
  void testValidatePropertyOwnership_Valid() {
    Users tenant = new Users();
    Long propertyId = 1L;
    Property property = new Property();
    property.setTenant(tenant);

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.of(property));

    Property result = validator.validatePropertyOwnership(tenant, propertyId);

    assertNotNull(result);
    assertEquals(property, result);
    verify(propertyService, times(1)).findPropertyById(propertyId);
  }

  @Test
  void testValidatePropertyOwnership_NotOwner() {
    Users tenant = new Users();
    Users otherUser = new Users();
    Long propertyId = 1L;
    Property property = new Property();
    property.setTenant(otherUser);

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.of(property));

    assertThrows(UnauthorizedOperationsException.class, () -> validator.validatePropertyOwnership(tenant, propertyId));
    verify(propertyService, times(1)).findPropertyById(propertyId);
  }

  @Test
  void testValidatePropertyOwnership_PropertyNotFound() {
    Users tenant = new Users();
    Long propertyId = 1L;

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.empty());

    assertThrows(PropertyNotFoundException.class, () -> validator.validatePropertyOwnership(tenant, propertyId));
    verify(propertyService, times(1)).findPropertyById(propertyId);
  }
}
