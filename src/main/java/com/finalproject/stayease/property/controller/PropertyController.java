package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.property.entity.dto.CategoryDTO;
import com.finalproject.stayease.property.entity.dto.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.PropertyDTO;
import com.finalproject.stayease.property.entity.dto.RoomDTO;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping("/create")
  public ResponseEntity<Response<PropertyDTO>> addProperty(@RequestBody CreatePropertyRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Property added!",
        new PropertyDTO(propertyService.createProperty(tenant, requestDTO)));
  }

  @PostMapping("/categories/create")
  public ResponseEntity<Response<CategoryDTO>> addCategory(@RequestBody CreateCategoryRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Category added!", new CategoryDTO(
        propertyCategoryService.createCategory(tenant, requestDTO)));
  }

  @PostMapping("/rooms/create")
  public ResponseEntity<Response<RoomDTO>> addRoom(@RequestBody CreateRoomRequestDTO requestDTO) {
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Room added!",
        new RoomDTO(roomService.createRoom(requestDTO)));
  }
}
