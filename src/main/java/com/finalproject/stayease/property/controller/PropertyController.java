package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.property.entity.dto.CategoryDTO;
import com.finalproject.stayease.property.entity.dto.PropertyDTO;
import com.finalproject.stayease.property.entity.dto.RoomDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @PostMapping
  public ResponseEntity<Response<PropertyDTO>> addProperty(@RequestBody CreatePropertyRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Property added!",
        new PropertyDTO(propertyService.createProperty(tenant, requestDTO)));
  }

  // Region - Property Categories

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
    return Response.successfulResponse(HttpStatus.OK.value(), "Category successfully deleted!", null);
  }

  // Region - Room

  @PostMapping("/rooms")
  public ResponseEntity<Response<RoomDTO>> addRoom(@RequestBody CreateRoomRequestDTO requestDTO) {
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Room added!",
        new RoomDTO(roomService.createRoom(requestDTO)));
  }
}
