package com.finalproject.stayease.auth.model.dto.forgorPassword.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordResponseDTO {
  private String message;
  private String token;
}
