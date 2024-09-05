package com.finalproject.stayease.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDto {
  private String accessToken;
  private String refreshToken;
}
