package com.finalproject.stayease.auth.service;

public interface RegisterRedisService {
  void saveVericationToken(String email, String verificationToken);
  void verifiedEmail(String email, String verificationToken);
  boolean isValid(String email, String verificationToken);

  // getters
  String getToken(String email);
  String getEmail(String verificationToken);
}
