package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.properties.DuplicateRoomException;
import com.finalproject.stayease.exceptions.properties.RoomNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.PropertyCurrentDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.property.repository.RoomRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private PropertyService propertyService;

  @Mock
  private PeakSeasonRateService peakSeasonRateService;

  @InjectMocks
  private RoomServiceImpl roomService;

  private Property property;
  private Room room;
  private CreateRoomRequestDTO createRoomDTO;
  private UpdateRoomRequestDTO updateRoomDTO;
  private Users tenant;

  @BeforeEach
  void setUp() {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setId(1L);
    tenantInfo.setBusinessName("Test Business");

    PropertyCategory category = new PropertyCategory();
    category.setId(1L);
    category.setName("Test Category");

    property = new Property();
    property.setId(1L);

    room = new Room();
    room.setId(1L);
    room.setProperty(property);
    room.setName("Test Room");

    createRoomDTO = new CreateRoomRequestDTO();
    createRoomDTO.setName("New Room");

    updateRoomDTO = new UpdateRoomRequestDTO();
    updateRoomDTO.setName("Updated Room");

    tenant = new Users();
    tenant.setId(1L);
    tenant.setTenantInfo(tenantInfo);
    tenantInfo.setUser(tenant);
    property.setTenant(tenant);
    property.setCategory(category);
  }

  @Test
  void getRoomsOfProperty_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findAllByPropertyAndDeletedAtIsNull(property)).thenReturn(Collections.singletonList(room));

    List<Room> result = roomService.getRoomsOfProperty(1L);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void getRoomsOfProperty_NoRooms() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findAllByPropertyAndDeletedAtIsNull(property)).thenReturn(Collections.emptyList());

    assertThrows(RoomNotFoundException.class, () -> roomService.getRoomsOfProperty(1L));
  }

  @Test
  void getUnavailableRoomsByPropertyIdAndDate_Success() {
    LocalDate date = LocalDate.now();
    when(roomRepository.findUnavailableRoomsByPropertyIdAndDate(1L, date)).thenReturn(Collections.singletonList(room));

    List<Room> result = roomService.getUnavailableRoomsByPropertyIdAndDate(1L, date);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void findRoomById_Success() {
    when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));

    Optional<Room> result = roomService.findRoomById(1L);
    assertTrue(result.isPresent());
    assertEquals(room.getId(), result.get().getId());
  }

  @Test
  void createRoom_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findAllRoomNamesByPropertyId(1L)).thenReturn(Collections.emptyList());
    when(roomRepository.save(any(Room.class))).thenReturn(room);

    Room result = roomService.createRoom(1L, createRoomDTO);
    assertNotNull(result);
    assertEquals(createRoomDTO.getName(), result.getName());
  }

  @Test
  void createRoom_DuplicateName() {
    when(roomRepository.findAllRoomNamesByPropertyId(1L)).thenReturn(List.of("New Room"));

    assertThrows(DuplicateRoomException.class, () -> roomService.createRoom(1L, createRoomDTO));
  }

  @Test
  void updateRoom_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));
    when(roomRepository.save(any(Room.class))).thenReturn(room);

    Room result = roomService.updateRoom(1L, 1L, updateRoomDTO);
    assertNotNull(result);
    assertEquals(updateRoomDTO.getName(), result.getName());
  }

  @Test
  void updateRoom_RoomNotFound() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(1L, 1L, updateRoomDTO));
  }

  @Test
  void deleteRoom_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));

    assertDoesNotThrow(() -> roomService.deleteRoom(1L, 1L));
    verify(roomRepository, times(1)).save(any(Room.class));
  }

  @Test
  void deletePropertyAndRoom_Success() {
    Set<Room> rooms = new HashSet<>(Collections.singletonList(room));
    property.setRooms(rooms);

    when(propertyService.deleteProperty(tenant, 1L)).thenReturn(property);

    Set<Room> result = roomService.deletePropertyAndRoom(tenant, 1L);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    verify(roomRepository, times(1)).save(any(Room.class));
  }

  @Test
  void getTenantRooms_Success() {
    when(roomRepository.findRoomByTenantIdAndDeletedAtIsNull(1L)).thenReturn(Collections.singletonList(room));

    List<Room> result = roomService.getTenantRooms(1L);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void getPropertyCurrent_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findAllByPropertyAndDeletedAtIsNull(property)).thenReturn(Collections.singletonList(room));

    PropertyCurrentDTO result = roomService.getPropertyCurrent(1L);
    assertNotNull(result);
    assertEquals(property.getName(), result.getPropertyName());
    assertFalse(result.getRooms().isEmpty());
  }

  @Test
  void getRoomRateAndAvailability_Success() {
    LocalDate date = LocalDate.now();
    RoomPriceRateDTO rateDTO = new RoomPriceRateDTO();
    rateDTO.setBasePrice(BigDecimal.valueOf(100));

    when(roomRepository.findRoomRateAndAvailability(1L, date)).thenReturn(rateDTO);
    when(peakSeasonRateService.applyPeakSeasonRate(rateDTO)).thenReturn(BigDecimal.valueOf(120));

    RoomAdjustedRatesDTO result = roomService.getRoomRateAndAvailability(1L, date);
    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(120), result.getAdjustedPrice());
  }
}