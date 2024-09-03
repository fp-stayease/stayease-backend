package com.finalproject.stayease.users.controller;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.auth.service.ResetPasswordService;
import com.finalproject.stayease.responses.Response;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Data
public class UsersController {

  private final ResetPasswordService resetPasswordService;

  @PostMapping("/reset-password")
  public ResponseEntity<Response<ForgotPasswordResponseDTO>> resetPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException {
    return Response.successfulResponse(HttpStatus.OK.value(), "Reset password requested!",
        resetPasswordService.requestResetTokenLoggedIn(requestDTO));
  }

  @PostMapping("/reset-password/set")
  public ResponseEntity<Response<Object>> resetPassword(@RequestParam String token, @Valid @RequestBody
  ResetPasswordRequestDTO requestDTO) {
    resetPasswordService.resetPassword(token, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Password successfully reset!", null);
  }
}
