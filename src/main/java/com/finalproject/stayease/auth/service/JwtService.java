package com.finalproject.stayease.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtService {

  String generateAccessToken(Authentication authentication);
  String generateAccessTokenFromEmail(String email);
  String generateRefreshToken(String email);
  String getToken(String email);
  String extractUsername(String token);
  void invalidateToken(String email);
  boolean isRefreshTokenValid(String email, String refreshToken);
  boolean isAccessTokenValid(String accessToken, String email);
  Jwt decodeToken(String token);
  Authentication getAuthenticationFromToken(String token);
}
