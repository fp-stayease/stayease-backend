package com.finalproject.stayease.auth.repository;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRedisRepository {

  private final static String STRING_KEY_PREFIX = "stayease:jwt:strings:";
  private final static String BLACKLIST_KEY_SUFFIX = ":blacklist";

  private final ValueOperations<String, String> valueOperations;

  public AuthRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
  }

  public void saveJwtKey(String email, String jwtKey) {
    valueOperations.set(STRING_KEY_PREFIX + email, jwtKey, 12, TimeUnit.HOURS);
  }

  public void blacklistKey(String email) {
    if (getJwtKey(email) == null) {
      // TODO : make TokenDoesNotExistException in AuthRedisRepository
      throw new RuntimeException("Token does not exist");
    }
    String key = STRING_KEY_PREFIX + email;
    Long remainingTTL = valueOperations.getOperations().getExpire(key, TimeUnit.SECONDS);
    valueOperations.set(STRING_KEY_PREFIX + email + BLACKLIST_KEY_SUFFIX, "true", remainingTTL, TimeUnit.SECONDS);
  }

  String getJwtKey(String email) {
    return valueOperations.get(STRING_KEY_PREFIX + email);
  }

  public boolean isValid (String email, String jwtKey) {
    String storedKey = valueOperations.get(STRING_KEY_PREFIX + email);
    String blacklisted = valueOperations.get(STRING_KEY_PREFIX + email + BLACKLIST_KEY_SUFFIX);
    return storedKey != null && blacklisted == null && storedKey.equals(jwtKey);
  }
}
