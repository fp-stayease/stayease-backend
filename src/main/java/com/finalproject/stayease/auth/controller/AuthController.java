package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.service.RegisterService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

  private final RegisterService registerService;

  @PostMapping("/register/user")
  public ResponseEntity<Response<InitialRegistrationResponseDTO>> initiateUserRegistration(@Valid @RequestBody InitialRegistrationRequestDTO requestDTO) {
    UserType userType = UserType.USER;
    return Response.successfulResponse(HttpStatus.OK.value(), "Initial 'User' registration successful!",
        registerService.initialRegistration(requestDTO, userType));
  }

  @PostMapping("/register/tenant")
  public ResponseEntity<Response<InitialRegistrationResponseDTO>> initiateTenantRegistration(@Valid @RequestBody InitialRegistrationRequestDTO requestDTO) {
    UserType userType = UserType.TENANT;
    return Response.successfulResponse(HttpStatus.OK.value(), "Initial 'Tenant' registration successful!",
        registerService.initialRegistration(requestDTO, userType));
  }

}
