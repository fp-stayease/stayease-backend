package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import io.jsonwebtoken.Claims;

public interface AuthService {

  TokenResponseDto login(LoginRequestDTO loginRequestDTO);

  void logout(String email);

  TokenResponseDto generateTokenFromEmail(String email);

  TokenResponseDto refreshAccessToken(String refreshToken);
}
