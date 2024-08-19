package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.repository.RegisterRedisRepository;
import com.finalproject.stayease.auth.service.RegisterRedisService;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class RegisterRedisServiceImpl implements RegisterRedisService {

  private final RegisterRedisRepository registerRedisRepository;

  @Override
  public void saveVericationToken(String email, String verificationToken) {
    registerRedisRepository.saveVerificationToken(email, verificationToken);
  }

  @Override
  public void verifyEmail(String email, String verificationToken) {
    registerRedisRepository.verifiedEmail(email, verificationToken);
  }

  @Override
  public boolean isValid(String email, String verificationToken) {
    return registerRedisRepository.isValid(email, verificationToken);
  }

  @Override
  public String getToken(String email) {
    return registerRedisRepository.getToken(email);
  }

  @Override
  public String getEmail(String verificationToken) {
    return registerRedisRepository.getEmail(verificationToken);
  }
}
