package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtService {

  String generateToken(User user);
  void invalidateToken(String email);
  boolean isTokenValid(String token, String email);
  Jwt decodeToken(String token);
  Authentication getAuthentication(String token);

}
