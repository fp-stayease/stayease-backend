package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.service.RegisterService;
import com.finalproject.stayease.responses.Response;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/register")
@Data
@Slf4j
public class RegistrationController {

  private final RegisterService registerService;

  @PostMapping
  public ResponseEntity<Response<InitialRegistrationResponseDTO>> initiateUserRegistration(@RequestParam("userType") String type, @Valid @RequestBody InitialRegistrationRequestDTO requestDTO) throws MessagingException, IOException {
    UserType userType = UserType.valueOf(type.toUpperCase());
    return Response.successfulResponse(HttpStatus.OK.value(), "Initial " + userType + " registration successful!", registerService.initialRegistration(requestDTO, userType));
  }

  @PostMapping("/checkToken")
  public ResponseEntity<Response<Boolean>> checkToken(@RequestParam String token) {
    Boolean isValid = registerService.checkToken(token);
    if (isValid) {
      return Response.successfulResponse(HttpStatus.OK.value(), "Token is valid", true);
    } else {
      return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Token is invalid, please check your email. Or "
                                                                     + "try to resend a registration request.",
          false);
    }
  }

  @PostMapping("/verify")
  public ResponseEntity<Response<VerifyUserResponseDTO>> verifyRegistration(@RequestParam String token, @Valid @RequestBody VerifyRegistrationDTO verifyRegistrationDTO) {
    return Response.successfulResponse(HttpStatus.ACCEPTED.value(), "Verification successful, welcome to StayEase! "
                                                                    + "Please login with your new credentials!",
        registerService.verifyRegistration(verifyRegistrationDTO, token));
  }
}