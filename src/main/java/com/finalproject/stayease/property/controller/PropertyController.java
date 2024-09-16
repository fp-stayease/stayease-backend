package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.CategoryDTO;
import com.finalproject.stayease.property.entity.dto.PeakSeasonRateDTO;
import com.finalproject.stayease.property.entity.dto.PropertyDTO;
import com.finalproject.stayease.property.entity.dto.RoomDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import java.util.List;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
@Data
public class PropertyController {

  private final UsersService usersService;
  private final PropertyService propertyService;
  private final PropertyCategoryService propertyCategoryService;
  private final RoomService roomService;
  private final PeakSeasonRateService peakSeasonRateService;

  @GetMapping
  public ResponseEntity<Response<List<PropertyDTO>>> getAllProperties() {
    List<Property> propertyList = propertyService.findAll();
    List<PropertyDTO> propertyDTOList = propertyList.stream().map(PropertyDTO::new).toList();
    return Response.successfulResponse(200, "Listing all properties", propertyDTOList);
  }

  @GetMapping("/{propertyId}")
  public ResponseEntity<Response<PropertyDTO>> getProperty(@PathVariable Long propertyId) {
    // TODO : PropertyNotFoundException
    Property property = propertyService.findPropertyById(propertyId).orElseThrow(() -> new DataNotFoundException("No property "
                                                                                                       + "is found "
                                                                                                       + "with ID: " + propertyId));
    return Response.successfulResponse(200, "Listing property ID: " + propertyId, new PropertyDTO(property));
  }

  @GetMapping("/tenant")
  public ResponseEntity<Response<List<PropertyDTO>>> getAllTenantProperties() {
    Users tenant = usersService.getLoggedUser();
    List<Property> tenantsProperties = propertyService.findAllByTenant(tenant);
    List<PropertyDTO> propertyDTOList = tenantsProperties.stream().map(PropertyDTO::new).toList();
    return Response.successfulResponse(200, "Listing tenant properties", propertyDTOList);
  }

  @PostMapping
  public ResponseEntity<Response<PropertyDTO>> addProperty(@RequestBody CreatePropertyRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Property added!",
        new PropertyDTO(propertyService.createProperty(tenant, requestDTO)));
  }

  @PutMapping("/{propertyId}")
  public ResponseEntity<Response<PropertyDTO>> updateProperty(@PathVariable Long propertyId,
      @RequestBody UpdatePropertyRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Property updated successfully!",
        new PropertyDTO(propertyService.updateProperty(tenant, propertyId, requestDTO)));
  }

  @DeleteMapping("/{propertyId}")
  public ResponseEntity<Response<Object>> deleteProperty(@PathVariable Long propertyId) {
    Users tenant = usersService.getLoggedUser();
    propertyService.deleteProperty(tenant, propertyId);
    return Response.successfulResponse(HttpStatus.OK.value(), "Property deleted successfully", null);
  }

  // Region - Property Categories

  @GetMapping("/categories")
  public ResponseEntity<Response<List<CategoryDTO>>> getAllCategories() {
    List<PropertyCategory> categoryList = propertyCategoryService.findAll();
    List<CategoryDTO> categoryDTOList = categoryList.stream().map(CategoryDTO::new).toList();
    return Response.successfulResponse(200, "Listing all categories", categoryDTOList);
  }

  @PostMapping("/categories")
  public ResponseEntity<Response<CategoryDTO>> addCategory(@RequestBody CreateCategoryRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Category added!", new CategoryDTO(
        propertyCategoryService.createCategory(tenant, requestDTO)));
  }

  @PutMapping("/categories/{categoryId}")
  public ResponseEntity<Response<CategoryDTO>> updateCategory(@PathVariable Long categoryId,
      @RequestBody UpdateCategoryRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Category updated!", new CategoryDTO(
        propertyCategoryService.updateCategory(categoryId, tenant, requestDTO)));
  }

  @DeleteMapping("/categories/{categoryId}")
  public ResponseEntity<Response<Object>> deleteCategory(@PathVariable Long categoryId) {
    Users tenant = usersService.getLoggedUser();
    propertyCategoryService.deleteCategory(categoryId, tenant);
    return Response.successfulResponse(HttpStatus.OK.value(), "Category successfully deleted!", null);
  }

  // Region - Room

  @GetMapping("/{propertyId}/rooms")
  public ResponseEntity<Response<List<RoomDTO>>> getAllRooms(@PathVariable Long propertyId) {
    List<Room> roomList = roomService.getRoomsOfProperty(propertyId);
    List<RoomDTO> roomDTOList = roomList.stream().map(RoomDTO::new).toList();
    return Response.successfulResponse(200, "Listing all rooms for property ID: " + propertyId, roomDTOList);
  }

  @PostMapping("/{propertyId}/rooms")
  public ResponseEntity<Response<RoomDTO>> addRoom(@PathVariable Long propertyId,
      @RequestBody CreateRoomRequestDTO requestDTO) {
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Room added!",
        new RoomDTO(roomService.createRoom(propertyId, requestDTO)));
  }

  @GetMapping("/{propertyId}/rooms/{roomId}")
  public ResponseEntity<Response<RoomDTO>> getRoom(@PathVariable Long propertyId, @PathVariable Long roomId) {
    Room room = roomService.getRoom(propertyId, roomId);
    return Response.successfulResponse(200, "Listing room ID: " + roomId, new RoomDTO(room));
  }

  @PutMapping("/{propertyId}/rooms/{roomId}")
  public ResponseEntity<Response<RoomDTO>> updateRoom(@PathVariable Long propertyId, @PathVariable Long roomId,
      @RequestBody UpdateRoomRequestDTO requestDTO) {
    return Response.successfulResponse(HttpStatus.OK.value(), "Room updated!",
        new RoomDTO(roomService.updateRoom(propertyId, roomId, requestDTO)));
  }

  @DeleteMapping("/{propertyId}/rooms/{roomId}")
  public ResponseEntity<Response<Object>> deleteRoom(@PathVariable Long propertyId, @PathVariable Long roomId) {
    roomService.deleteRoom(propertyId, roomId);
    return Response.successfulResponse(HttpStatus.OK.value(), "Room successfully deleted!", null);
  }

  // Region - PeakSeasonRate

  @PostMapping("/{propertyId}/rates")
  public ResponseEntity<Response<PeakSeasonRateDTO>> setPeakSeasonRate(@PathVariable Long propertyId,
      @RequestBody SetPeakSeasonRateRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Adjustment Rate Successfully Set!",
        new PeakSeasonRateDTO(peakSeasonRateService.setPeakSeasonRate(tenant, propertyId, requestDTO)));
  }

  @PostMapping("/{propertyId}/rates/{rateId}")
  public ResponseEntity<Response<PeakSeasonRateDTO>> updatePeakSeasonRate(@PathVariable Long propertyId,
      @PathVariable Long rateId,
      @RequestBody SetPeakSeasonRateRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Adjustment Rate Successfully Updated!",
        new PeakSeasonRateDTO(peakSeasonRateService.updatePeakSeasonRate(tenant, propertyId, rateId, requestDTO)));
  }
}
