package com.finalproject.stayease.auth.service;

public interface RegisterRedisService {
  void saveVericationToken(String email, String verificationToken);
  void verifiedEmail(String email);
  boolean isValid(String email, String verificationToken);
}
