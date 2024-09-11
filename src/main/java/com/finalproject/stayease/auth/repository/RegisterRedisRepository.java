package com.finalproject.stayease.auth.repository;

import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@Transactional
public class RegisterRedisRepository {

  public static final String VERIFICATION_TOKEN_PREFIX = "stayease:verification:strings:";
  public static final String TOKEN_TO_EMAIL_PREFIX = "stayease:token:email:";
  public static final String VERIFIED_SUFFIX = ":verified";

  private final ValueOperations<String, String> valueOperations;

  public RegisterRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
  }

  // * save verification token
  public void saveVerificationToken(String email, String verificationToken) {
    valueOperations.set(VERIFICATION_TOKEN_PREFIX + email, verificationToken, 1, TimeUnit.HOURS);
    valueOperations.set(TOKEN_TO_EMAIL_PREFIX + verificationToken, email, 1, TimeUnit.HOURS);
  }

  // * verified
  public void verifiedEmail(String email, String verificationToken) {
      valueOperations.set(VERIFICATION_TOKEN_PREFIX + email + VERIFIED_SUFFIX, "true", 1, TimeUnit.HOURS);
      valueOperations.set(TOKEN_TO_EMAIL_PREFIX + verificationToken + VERIFIED_SUFFIX, "true", 1, TimeUnit.HOURS);
  }

  // helpers
  public String getToken(String email) {
    return valueOperations.get(VERIFICATION_TOKEN_PREFIX + email);
  }

  public String getEmail(String token) {
    return valueOperations.get(TOKEN_TO_EMAIL_PREFIX + token);
  }

  public boolean isValid (String email, String verificationToken) {
    String token = getToken(email);
    String storedEmail = getEmail(verificationToken);
    String verified = valueOperations.get(VERIFICATION_TOKEN_PREFIX + email + VERIFIED_SUFFIX);
    String verifiedToken = valueOperations.get(TOKEN_TO_EMAIL_PREFIX + verificationToken + VERIFIED_SUFFIX);
    return token != null && storedEmail != null && verified == null && verifiedToken == null && token.equals(verificationToken) && storedEmail.equals(email);
  }

}
