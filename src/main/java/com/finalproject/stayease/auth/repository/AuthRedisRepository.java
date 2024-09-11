package com.finalproject.stayease.auth.repository;

import com.finalproject.stayease.exceptions.TokenDoesNotExistException;
import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@RedisHash
@Slf4j
@Transactional
public class AuthRedisRepository {

  private final static String STRING_KEY_PREFIX = "stayease:jwt:refresh:";
  private final static String BLACKLIST_KEY_PREFIX = "blacklist:";
  private final static Integer REFRESH_TOKEN_EXPIRE_DAYS = 7;

  private final ValueOperations<String, String> valueOperations;
  private final RedisTemplate<String, String> redisTemplate;

  public AuthRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
    this.redisTemplate = redisTemplate;
  }

  public void saveJwtKey(String email, String jwtKey) {
    String key = STRING_KEY_PREFIX + email;
    redisTemplate.delete(key); // make sure previous value deleted
    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      log.info("Previous token deleted for email: {}", email);
    }
    valueOperations.set(STRING_KEY_PREFIX + email, jwtKey, REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
    log.info("Saved refresh token for email: {}", email);
    log.info("Saved refresh token value: {}", jwtKey);
  }

  public void blacklistKey(String email) {
    if (getJwtKey(email) == null) {
      throw new TokenDoesNotExistException("Token does not exist");
    }
    String tokenFromEmail = getJwtKey(email);
    String key = STRING_KEY_PREFIX + BLACKLIST_KEY_PREFIX + tokenFromEmail;
    Long remainingTTL = redisTemplate.getExpire(STRING_KEY_PREFIX + email, TimeUnit.SECONDS);
    valueOperations.set(key, "true", remainingTTL, TimeUnit.SECONDS);
  }

  public String getJwtKey(String email) {
    return valueOperations.get(STRING_KEY_PREFIX + email);
  }

  public boolean isValid (String jwtKey, String email) {
    String storedKey = valueOperations.get(STRING_KEY_PREFIX + email);
    log.info("Validating refresh token for email: {}", email);
    log.info("Stored token: {}", storedKey);
    log.info("Provided token: {}", jwtKey);
    log.info("blacklist? " + isRefreshTokenBlacklisted(jwtKey));
    return storedKey != null && storedKey.equals(jwtKey) && !isRefreshTokenBlacklisted(jwtKey);
  }

  public boolean isRefreshTokenBlacklisted(String refreshToken) {
    String key = STRING_KEY_PREFIX + BLACKLIST_KEY_PREFIX + refreshToken;
    return getJwtKey(key) != null;
  }
}
