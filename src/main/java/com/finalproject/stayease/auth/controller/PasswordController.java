package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.auth.service.ResetPasswordService;
import com.finalproject.stayease.responses.Response;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping("/api/v1/password")
@Slf4j
public class PasswordController {

  private final ResetPasswordService resetPasswordService;

  @PostMapping("/forgot")
  public ResponseEntity<Response<ForgotPasswordResponseDTO>> forgotPassword(@RequestParam(required = false) boolean loggedIn, @Valid @RequestBody ForgotPasswordRequestDTO requestDTO) throws MessagingException, IOException {
    if (loggedIn) {
      return Response.successfulResponse(HttpStatus.OK.value(), "Reset password requested for logged in user!",
          resetPasswordService.requestResetTokenLoggedIn(requestDTO));
    } else {
      return Response.successfulResponse(HttpStatus.OK.value(), "Reset password requested!", resetPasswordService.requestResetToken(requestDTO));
    }
  }

  @PostMapping("/reset")
  public ResponseEntity<Response<Object>> resetPassword(@RequestParam String token, @Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
    resetPasswordService.resetPassword(token, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Password successfully reset!", null);
  }

  @PostMapping("/check-token")
  public ResponseEntity<Response<Boolean>> checkToken(@RequestBody String token) {
    String normalizedToken = token.replaceAll("=+$", "");
    boolean isValid = resetPasswordService.checkToken(normalizedToken);
    String responseMessage = isValid ? "Token is valid" : "Token is invalid, please check your email or try to resend"
                                                          + " a reset password request.";
    return Response.successfulResponse(HttpStatus.OK.value(), responseMessage, isValid);
  }


}
