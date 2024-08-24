package com.finalproject.stayease.auth.model.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
  private String message;
  private String accessToken;
  private String refreshToken;
}
