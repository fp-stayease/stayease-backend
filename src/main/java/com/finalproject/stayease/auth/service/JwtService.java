package com.finalproject.stayease.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtService {

  String generateAccessToken(Authentication authentication);
  String generateRefreshToken(Authentication authentication);
  String getToken(String email);
  void invalidateToken(String email);
  boolean isTokenValid(String token, String email);
  Jwt decodeToken(String token);
  Authentication getAuthentication(String token);

}
