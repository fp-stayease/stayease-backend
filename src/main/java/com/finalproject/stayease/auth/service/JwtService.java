package com.finalproject.stayease.auth.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtService {

  String generateAccessToken(Authentication authentication);
  String generateRefreshToken(Authentication authentication);
  String getToken(String email);
  String extractUsername(String token);
  void invalidateToken(String email);
  boolean isRefreshTokenValid(String token, String email);
  boolean isAccessTokenValid(String accessToken, String email);
  Jwt decodeToken(String token);
  Authentication getAuthenticationFromToken(String token);
}
