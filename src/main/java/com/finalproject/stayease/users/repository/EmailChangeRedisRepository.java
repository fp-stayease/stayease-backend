package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.exceptions.InvalidRequestException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class EmailChangeRedisRepository {

  public static final String EMAIL_CHANGE_TOKEN_PREFIX = "stayease:emailchange:strings:";
  public static final String VERIFIED_SUFFIX = ":verified";

  private final ValueOperations<String, String> valueOperations;

  @Value("${token.expiration.hours:1}")
  private int tokenExpirationHours;

  public EmailChangeRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
  }

  // * save verification token
  public void saveToken(String tokenUUID, String jwt) {
    String key = EMAIL_CHANGE_TOKEN_PREFIX + tokenUUID;
    valueOperations.set(key, jwt, tokenExpirationHours, TimeUnit.HOURS);
  }

  // * verify
  public void verifyEmail(String tokenUUID) {
    String key = EMAIL_CHANGE_TOKEN_PREFIX + tokenUUID;
    valueOperations.set(key + VERIFIED_SUFFIX, "true", tokenExpirationHours, TimeUnit.HOURS);
  }

  // helpers
  public String getJwt(String tokenUUID) {
    String key = EMAIL_CHANGE_TOKEN_PREFIX + tokenUUID;
    String isVerified = valueOperations.get(key + VERIFIED_SUFFIX);
    if (isVerified != null) {
      throw new InvalidRequestException("This email change request has already been verified");
    }
    return valueOperations.get(key);
  }

  public boolean isValid (String tokenUUID) {
    String key = EMAIL_CHANGE_TOKEN_PREFIX + tokenUUID + VERIFIED_SUFFIX;
    return valueOperations.get(key) == null;
  }
}
