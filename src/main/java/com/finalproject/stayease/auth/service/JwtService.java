package com.finalproject.stayease.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtService {

  String generateAccessToken(Authentication authentication);
  String generateAccessTokenFromEmail(String email);
  String generateRefreshToken(String email);
  String extractSubjectFromToken(String token);
  void invalidateToken(String email);
  boolean isRefreshTokenValid(String refreshToken, String email);
  boolean isAccessTokenValid(String token, String email);
  Jwt decodeToken(String token);
}
