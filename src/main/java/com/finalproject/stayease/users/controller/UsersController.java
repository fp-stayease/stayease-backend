package com.finalproject.stayease.users.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.register.UserProfileDTO;
import com.finalproject.stayease.users.service.UsersService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Data
public class UsersController {

  private final UsersService usersService;

  @GetMapping
  public ResponseEntity<Response<UserProfileDTO>> getLoggedInProfile() {
    Users loggedUser = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Profile retrieved successfully!", new UserProfileDTO(loggedUser));
  }

}
