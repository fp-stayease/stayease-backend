package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.properties.DuplicateRoomException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.properties.RoomNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.property.repository.RoomRepository;
import com.finalproject.stayease.property.service.PropertyService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private PropertyService propertyService;

  @InjectMocks
  private RoomServiceImpl roomService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateRoom_ValidRequest() {
    CreateRoomRequestDTO requestDTO = new CreateRoomRequestDTO();
    requestDTO.setName("Room A");
    requestDTO.setDescription("Cozy room");
    requestDTO.setBasePrice(BigDecimal.valueOf(100000));
    requestDTO.setCapacity(2);

    Long propertyId = 1L;

    Property property = new Property();
    property.setId(1L);

    Room expectedRoom = new Room();
    expectedRoom.setName("Room A");
    expectedRoom.setDescription("Cozy room");
    expectedRoom.setBasePrice(BigDecimal.valueOf(100000));
    expectedRoom.setCapacity(2);
    expectedRoom.setProperty(property);

    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.save(any(Room.class))).thenReturn(expectedRoom);

    Room createdRoom = roomService.createRoom(propertyId, requestDTO);

    assertEquals(expectedRoom.getName(), createdRoom.getName());
    assertEquals(expectedRoom.getId(), createdRoom.getId());
    verify(propertyService, times(1)).findPropertyById(1L);
    verify(roomRepository, times(1)).save(any(Room.class));
  }

  @Test
  void testCreateRoom_DuplicateRoomName() {
    CreateRoomRequestDTO requestDTO = new CreateRoomRequestDTO();
    requestDTO.setName("Room A");
    requestDTO.setDescription("Cozy room");
    requestDTO.setBasePrice(BigDecimal.valueOf(100000));
    requestDTO.setCapacity(2);

    Long propertyId = 1L;

    Room existingRoom = new Room();
    existingRoom.setName("Room A");

    when(roomRepository.findByNameIgnoreCaseAndDeletedAtIsNull("Room A")).thenReturn(Optional.of(existingRoom));

    assertThrows(DuplicateRoomException.class, () -> roomService.createRoom(propertyId, requestDTO));
    verify(roomRepository, times(1)).findByNameIgnoreCaseAndDeletedAtIsNull("Room A");
  }

  @Test
  void testCreateRoom_PropertyNotFound() {
    CreateRoomRequestDTO requestDTO = new CreateRoomRequestDTO();
    requestDTO.setName("Room A");
    requestDTO.setDescription("Cozy room");
    requestDTO.setBasePrice(BigDecimal.valueOf(100000));
    requestDTO.setCapacity(2);

    Long propertyId = 1L;

    when(propertyService.findPropertyById(1L)).thenReturn(Optional.empty());

    assertThrows(PropertyNotFoundException.class, () -> roomService.createRoom(propertyId, requestDTO));
    verify(propertyService, times(1)).findPropertyById(1L);
  }

  @Test
  void testUpdateRoom_ValidRequest() {
    UpdateRoomRequestDTO requestDTO = new UpdateRoomRequestDTO();
    requestDTO.setName("Room A");
    requestDTO.setDescription("Cozy room");
    requestDTO.setBasePrice(BigDecimal.valueOf(100000));
    requestDTO.setCapacity(2);

    Long roomId = 1L;

    Long propertyId = 1L;
    Property property = new Property();
    property.setId(propertyId);

    Room existingRoom = new Room();
    existingRoom.setId(roomId);
    existingRoom.setName("Room 1");
    existingRoom.setProperty(property);

    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findByIdAndDeletedAtIsNull(roomId)).thenReturn(Optional.of(existingRoom));
    Room expectedRoom = roomService.updateRoom(propertyId, roomId, requestDTO);

    assertEquals(existingRoom.getId(), expectedRoom.getId());
    assertEquals(existingRoom.getName(), "Room A");
  }

  @Test
  void testUpdateRoom_RoomDoesNotExist() {
    Long roomId = 1L;

    Long propertyId = 1L;

    when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(propertyId, roomId,
        new UpdateRoomRequestDTO()));
  }

  @Test
  void testDeleteRoom() {
    //Arrange
    Long roomId = 1L;

    Long propertyId = 1L;
    Property property = new Property();
    property.setId(propertyId);

    Room existingRoom = new Room();
    existingRoom.setId(roomId);
    existingRoom.setProperty(property);

    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(roomRepository.findByIdAndDeletedAtIsNull(roomId)).thenReturn(Optional.of(existingRoom));

    // Act
    roomService.deleteRoom(propertyId, roomId);

    // Assert
    verify(roomRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
    verify(roomRepository, times(1)).save(any(Room.class));
    assertNotNull(existingRoom.getDeletedAt());
  }
}
