package com.finalproject.stayease.auth.repository;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RegisterRedisRepository {

  public static final String VERIFICATION_TOKEN_PREFIX = "stayease:verification:strings:";
  public static final String VERIFIED_SUFFIX = ":verified";

  private final ValueOperations<String, String> valueOperations;

  public RegisterRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
  }

  // * save verification token
  public void saveVerificationToken(String email, String verificationToken) {
    valueOperations.set(VERIFICATION_TOKEN_PREFIX + email, verificationToken, 1, TimeUnit.HOURS);
  }

  // * verified
  public void verifiedEmail(String email) {
      valueOperations.set(VERIFICATION_TOKEN_PREFIX + email + VERIFIED_SUFFIX, "true", 1, TimeUnit.HOURS);
  }

  // helpers
  public String getToken(String email) {
    return valueOperations.get(VERIFICATION_TOKEN_PREFIX + email);
  }

  public boolean isValid (String email, String verificationToken) {
    String token = getToken(email);
    String verified = valueOperations.get(VERIFICATION_TOKEN_PREFIX + email + VERIFIED_SUFFIX);
    return token != null && verified == null && token.equals(verificationToken);
  }

}
