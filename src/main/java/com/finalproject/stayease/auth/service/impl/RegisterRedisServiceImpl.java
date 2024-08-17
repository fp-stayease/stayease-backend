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
  public void verifiedEmail(String email) {
    registerRedisRepository.verifiedEmail(email);
  }

  @Override
  public boolean isValid(String email, String verificationToken) {
    return registerRedisRepository.isValid(email, verificationToken);
  }
}
