package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.entity.dto.CategoryDTO;
import com.finalproject.stayease.property.entity.dto.PropertyCurrentDTO;
import com.finalproject.stayease.property.entity.dto.PropertyDTO;
import com.finalproject.stayease.property.entity.dto.PropertyRoomImageDTO;
import com.finalproject.stayease.property.entity.dto.RoomAvailabilityDTO;
import com.finalproject.stayease.property.entity.dto.RoomDTO;
import com.finalproject.stayease.property.entity.dto.RoomWithRoomAvailabilityDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetUnavailabilityDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyAvailableOnDateDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.PropertyImageUploadService;
import com.finalproject.stayease.property.service.PropertyListingService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomAvailabilityService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/properties")
@Data
@Slf4j
public class PropertyController {

  private final UsersService usersService;
  private final PropertyService propertyService;
  private final PropertyCategoryService propertyCategoryService;
  private final RoomService roomService;
  private final PeakSeasonRateService peakSeasonRateService;
  private final PropertyImageUploadService propertyImageUploadService;
  private final PropertyListingService propertyListingService;
  private final RoomAvailabilityService roomAvailabilityService;


  @GetMapping
  public ResponseEntity<Response<List<PropertyDTO>>> getAllProperties() {
    List<Property> propertyList = propertyService.findAll();
    List<PropertyDTO> propertyDTOList = propertyList.stream().map(PropertyDTO::new).toList();
    return Response.successfulResponse(200, "Listing all properties", propertyDTOList);
  }

  @GetMapping("/listings")
  public ResponseEntity<Response<Map<String, Object>>> getPropertiesListings(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) String city,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String searchTerm,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection
  ) {
    Page<PropertyListingDTO> properties = propertyListingService.findAvailableProperties(
        startDate, endDate, city, categoryId, searchTerm, minPrice,
        maxPrice, page, size,
        sortBy, sortDirection);

    return Response.responseMapper(
        HttpStatus.OK.value(),
        "Listing available properties",
        properties
    );
  }

  @GetMapping("/available")
  public ResponseEntity<Response<List<PropertyListingDTO>>> getAvailableProperties(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    List<PropertyListingDTO> properties = propertyListingService.findPropertiesWithLowestRoomRate(date);
    return Response.successfulResponse(200, "Listing available properties", properties);
  }

  @GetMapping("/{propertyId}")
  public ResponseEntity<Response<PropertyCurrentDTO>> getProperty(@PathVariable Long propertyId) {
    return Response.successfulResponse(200, "Listing property ID: " + propertyId, roomService.getPropertyCurrent(propertyId));
  }

  @GetMapping("{propertyId}/available")
   public ResponseEntity<Response<PropertyAvailableOnDateDTO>> getAvailablePropertyOnDate(@PathVariable Long propertyId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return Response.successfulResponse(200, "Listing available property for today",
        propertyListingService.findAvailablePropertyOnDate(propertyId, date));
  }

  @GetMapping("/tenant")
  public ResponseEntity<Response<List<PropertyDTO>>> getAllTenantProperties() {
    Users tenant = usersService.getLoggedUser();
    List<Property> tenantsProperties = propertyService.findAllByTenant(tenant);
    List<PropertyDTO> propertyDTOList = tenantsProperties.stream().map(PropertyDTO::new).toList();
    return Response.successfulResponse(200, "Listing tenant properties", propertyDTOList);
  }

  @GetMapping("/tenant/rooms")
  public ResponseEntity<Response<List<RoomDTO>>> getAllTenantRooms() {
    Users tenant = usersService.getLoggedUser();
    List<Room> tenantRooms = roomService.getTenantRooms(tenant.getId());
    List<RoomDTO> roomDTOList = tenantRooms.stream().map(RoomDTO::new).toList();
    return Response.successfulResponse(200, "Listing tenant rooms", roomDTOList);
  }

  @PostMapping
  public ResponseEntity<Response<PropertyDTO>> addProperty(@RequestBody CreatePropertyRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Property added!",
        new PropertyDTO(propertyService.createProperty(tenant, requestDTO)));
  }

  @PostMapping("/upload-image")
  public ResponseEntity<Response<PropertyRoomImageDTO>> uploadPropertyImage(@RequestBody MultipartFile image) throws IOException {
    Users tenant = usersService.getLoggedUser();
    Long id = tenant.getId();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Property image uploaded!",
        new PropertyRoomImageDTO(propertyImageUploadService.uploadImage(id, image)));
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

  @GetMapping("/{propertyId}/rooms/{roomId}/available")
  public ResponseEntity<Response<RoomAdjustedRatesDTO>> getRoom(@PathVariable Long propertyId,
      @PathVariable Long roomId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    Room room = roomService.getRoom(propertyId, roomId);
    return Response.successfulResponse(200, "Listing room ID: " + roomId, roomService.getRoomRateAndAvailability(roomId, date));
  }

  @PostMapping("/{propertyId}/rooms/{roomId}")
  public ResponseEntity<Response<PropertyRoomImageDTO>> uploadRoomImage(@PathVariable Long propertyId,
      @PathVariable Long roomId, @RequestBody MultipartFile image) throws IOException {
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Room image uploaded!",
        new PropertyRoomImageDTO(propertyImageUploadService.uploadRoomImage(propertyId, roomId, image)));
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

  // Region - RoomAvailability

  @GetMapping("/tenant/availability")
  public ResponseEntity<Response<List<RoomWithRoomAvailabilityDTO>>> getTenantRoomAvailability() {
    Users tenant = usersService.getLoggedUser();
    List<Room> availabilities = roomService.getRoomsAvailability(tenant.getId());
    List<RoomWithRoomAvailabilityDTO> response = availabilities.stream().map(RoomWithRoomAvailabilityDTO::new).toList();
    return Response.successfulResponse(200, "Listing availability for tenant ID: " + tenant.getId(), response);
  }

  @PostMapping("/{propertyId}/rooms/{roomId}/unavailable")
  public ResponseEntity<Response<RoomAvailabilityDTO>> setRoomUnavailability(@PathVariable Long propertyId,
      @PathVariable Long roomId,
      @RequestBody SetUnavailabilityDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    RoomAvailability availability = roomAvailabilityService.setUnavailability(tenant, roomId, requestDTO.getStartDate(),
        requestDTO.getEndDate());
    log.info("Room unavailability set for room ID: " + roomId);
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Room unavailability set!",
        new RoomAvailabilityDTO(availability));
  }

  @DeleteMapping("/{propertyId}/rooms/{roomId}/unavailable/{availabilityId}")
  public ResponseEntity<Response<Object>> removeRoomUnavailability(@PathVariable Long propertyId,
      @PathVariable Long roomId,
      @PathVariable Long availabilityId) {
    Users tenant = usersService.getLoggedUser();
    roomAvailabilityService.removeUnavailability(tenant, roomId, availabilityId);
    log.info("Room unavailability removed for room ID: " + roomId);
    return Response.successfulResponse(HttpStatus.OK.value(), "Room unavailability removed!", null);
  }


  // Region - utilities
  @GetMapping("/cities")
  public ResponseEntity<Response<List<String>>> getDistinctCities() {
    return Response.successfulResponse(200, "Listing all distinct cities", propertyService.findDistinctCities());
  }

  @GetMapping("/images")
  public ResponseEntity<Response<List<String>>> getAllPropertyRoomImageUrls() {
    return Response.successfulResponse(200, "Listing all property and room image URLs",
        propertyService.findAllPropertyRoomImageUrls());
  }

  @GetMapping("/{propertyId}/check-ownership")
  public ResponseEntity<Response<Boolean>> checkOwnership(@PathVariable Long propertyId) {
    Users tenant = usersService.getLoggedUser();
    Boolean isOwner = propertyService.isTenantPropertyOwner(tenant, propertyId);
    log.info("Checking if tenant is owner of property ID: " + isOwner);
    return Response.successfulResponse(200, "Checking if tenant is owner of property ID: " + propertyId,
        isOwner);
  }
}
