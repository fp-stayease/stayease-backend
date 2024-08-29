package com.finalproject.stayease.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
  private String accessToken;
  private String refreshToken;
}
