package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.RoomAvailabilityNotFoundException;
import com.finalproject.stayease.exceptions.properties.RoomNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.entity.dto.RoomWithRoomAvailabilityDTO;
import com.finalproject.stayease.property.repository.RoomAvailabilityRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoomAvailabilityServiceImplTest {

  @Mock
  private RoomAvailabilityRepository roomAvailabilityRepository;

  @Mock
  private RoomService roomService;

  @Mock
  private PropertyService propertyService;

  @InjectMocks
  private RoomAvailabilityServiceImpl roomAvailabilityService;

  private Users tenant;
  private TenantInfo tenantInfo;
  private Property property;
  private Room room;
  private RoomAvailability roomAvailability;

  @BeforeEach
  void setUp() {
    tenant = new Users();
    tenant.setId(1L);
    tenant.setUserType(Users.UserType.TENANT);
    tenant.setTenantInfo(tenantInfo);

    tenantInfo = new TenantInfo();
    tenantInfo.setId(1L);
    tenantInfo.setUser(tenant);
    tenantInfo.setBusinessName("Business Name");

    property = new Property();
    property.setId(1L);
    property.setTenant(tenant);

    room = new Room();
    room.setId(1L);
    room.setProperty(property);

    roomAvailability = new RoomAvailability();
    roomAvailability.setId(1L);
    roomAvailability.setRoom(room);
    roomAvailability.setStartDate(LocalDate.now());
    roomAvailability.setEndDate(LocalDate.now().plusDays(1));
    roomAvailability.setIsAvailable(false);
    roomAvailability.setIsManual(true);
  }

  @Test
  void setUnavailability_Success() {
    when(roomService.findRoomById(1L)).thenReturn(Optional.of(room));
    when(roomAvailabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

    RoomAvailability result = roomAvailabilityService.setUnavailability(1L, LocalDate.now(), LocalDate.now().plusDays(1));

    assertNotNull(result);
    assertEquals(roomAvailability, result);
  }

  @Test
  void setUnavailability_RoomNotFound() {
    lenient().when(roomService.findRoomById(1L)).thenReturn(Optional.empty());

    assertThrows(RoomNotFoundException.class, () -> roomAvailabilityService.setUnavailability(1L, LocalDate.now(),
        LocalDate.now().plusDays(1)));
  }

  @Test
  void setUnavailability_Tenant_Success() {
    when(roomService.findRoomById(1L)).thenReturn(Optional.of(room));
    when(roomAvailabilityRepository.existsOverlappingAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
    when(roomAvailabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

    RoomAvailability result = roomAvailabilityService.setUnavailability(tenant, 1L, LocalDate.now(), LocalDate.now().plusDays(1));

    assertNotNull(result);
    assertEquals(roomAvailability, result);
  }

  @Test
  void setUnavailability_Tenant_Unauthorized() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    property.setTenant(otherTenant);

    when(roomService.findRoomById(1L)).thenReturn(Optional.of(room));

    assertThrows(UnauthorizedOperationsException.class, () -> roomAvailabilityService.setUnavailability(tenant, 1L, LocalDate.now(),
        LocalDate.now().plusDays(1)));
  }

  @Test
  void removeUnavailability_Success() {
    when(roomAvailabilityRepository.findByRoomIdAndDates(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(Optional.of(roomAvailability));

    assertDoesNotThrow(() -> roomAvailabilityService.removeUnavailability(1L, LocalDate.now(), LocalDate.now().plusDays(1)));
    verify(roomAvailabilityRepository).save(roomAvailability);
  }

  @Test
  void removeUnavailability_NotFound() {
    when(roomAvailabilityRepository.findByRoomIdAndDates(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(Optional.empty());

    assertThrows(
        RoomAvailabilityNotFoundException.class, () -> roomAvailabilityService.removeUnavailability(1L, LocalDate.now(),
        LocalDate.now().plusDays(1)));
  }

  @Test
  void removeUnavailability_Tenant_Success() {
    when(roomAvailabilityRepository.findById(1L)).thenReturn(Optional.of(roomAvailability));

    assertDoesNotThrow(() -> roomAvailabilityService.removeUnavailability(tenant, 1L, 1L));
    verify(roomAvailabilityRepository).save(roomAvailability);
  }

  @Test
  void removeUnavailability_Tenant_Unauthorized() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    property.setTenant(otherTenant);

    when(roomAvailabilityRepository.findById(1L)).thenReturn(Optional.of(roomAvailability));

    assertThrows(
        UnauthorizedOperationsException.class, () -> roomAvailabilityService.removeUnavailability(tenant, 1L, 1L));
  }

  @Test
  void getRoomAvailabilityByTenant_Success() {
    List<Property> properties = Collections.singletonList(property);
    List<Room> rooms = Collections.singletonList(room);
    List<RoomAvailability> availabilities = Collections.singletonList(roomAvailability);

    when(propertyService.findAllByTenant(tenant)).thenReturn(properties);
    when(roomService.getRoomsOfProperty(1L)).thenReturn(rooms);
    when(roomAvailabilityRepository.findAllByPropertyId(1L)).thenReturn(availabilities);

    List<RoomWithRoomAvailabilityDTO> result = roomAvailabilityService.getRoomAvailabilityByTenant(tenant);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void getRoomAvailabilityByTenant_NoAvailabilities() {
    List<Property> properties = Collections.singletonList(property);
    List<Room> rooms = Collections.singletonList(room);

    when(propertyService.findAllByTenant(tenant)).thenReturn(properties);
    when(roomService.getRoomsOfProperty(1L)).thenReturn(rooms);
    when(roomAvailabilityRepository.findAllByPropertyId(1L)).thenReturn(Collections.emptyList());

    List<RoomWithRoomAvailabilityDTO> result = roomAvailabilityService.getRoomAvailabilityByTenant(tenant);

    assertTrue(result.isEmpty());
  }

  @Test
  void removeUnavailabilityByRoomsDeletedAtNotNull_Success() {
    Set<Room> rooms = new HashSet<>(Collections.singletonList(room));
    room.setRoomAvailabilities(new HashSet<>(Collections.singletonList(roomAvailability)));

    when(roomAvailabilityRepository.findAllByPropertyIdAndIsManualFalse(1L)).thenReturn(Collections.emptyList());
    when(roomService.softDeletePropertyAndRoom(tenant, 1L)).thenReturn(rooms);

    assertDoesNotThrow(() -> roomAvailabilityService.removeUnavailabilityByRoomsDeletedAtNotNull(tenant, 1L));
    verify(roomAvailabilityRepository).save(roomAvailability);
  }

  @Test
  void removeUnavailabilityByRoomsDeletedAtNotNull_BookedRooms() {
    when(roomAvailabilityRepository.findAllByPropertyIdAndIsManualFalse(1L)).thenReturn(Collections.singletonList(roomAvailability));

    assertThrows(InvalidRequestException.class,
        () -> roomAvailabilityService.removeUnavailabilityByRoomsDeletedAtNotNull(tenant, 1L));
  }

  @Test
  void validateDateRange_InvalidDates() {
    when(roomService.findRoomById(1L)).thenReturn(Optional.of(room));

    assertThrows(InvalidDateException.class, () -> roomAvailabilityService.setUnavailability(tenant, 1L,
        LocalDate.now().plusDays(1), LocalDate.now()));
  }

  @Test
  void validateDateRange_PastDate() {
    when(roomService.findRoomById(1L)).thenReturn(Optional.of(room));

    assertThrows(InvalidDateException.class, () -> roomAvailabilityService.setUnavailability(tenant, 1L,
        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
  }

  @Test
  void validateDateRange_OverlappingDates() {
    when(roomService.findRoomById(1L)).thenReturn(Optional.of(room));
    when(roomAvailabilityRepository.existsOverlappingAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

    assertThrows(InvalidDateException.class, () -> roomAvailabilityService.setUnavailability(tenant, 1L, LocalDate.now(),
        LocalDate.now().plusDays(1)));
  }
}
