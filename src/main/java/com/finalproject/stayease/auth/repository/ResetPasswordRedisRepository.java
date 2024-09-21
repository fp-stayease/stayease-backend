package com.finalproject.stayease.auth.repository;

import com.finalproject.stayease.exceptions.TokenDoesNotExistException;
import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@Transactional
public class ResetPasswordRedisRepository {

  private static final String RESET_PASSWORD_KEY_PREFIX = "stayease:password:reset:";
  private static final String EMAIL_PREFIX = "email:";
  private static final String BLACKLIST_PREFIX = "blacklist:";
  private static final int TOKEN_EXPIRE = 1 * 60 * 60;

  private final ValueOperations<String, String> valueOperations;
  private final RedisTemplate<String, String> redisTemplate;

  public ResetPasswordRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
    this.redisTemplate = redisTemplate;
  }

  public void saveResetToken(String email, String token, String jwtKey) {
    String key = RESET_PASSWORD_KEY_PREFIX + token;
    String emailKey =  RESET_PASSWORD_KEY_PREFIX + EMAIL_PREFIX + email;
    valueOperations.set(key, jwtKey, TOKEN_EXPIRE, TimeUnit.SECONDS);
    valueOperations.set(emailKey, token, TOKEN_EXPIRE, TimeUnit.SECONDS);
  }

  public boolean isRequested(String email) {
    String emailKey =  RESET_PASSWORD_KEY_PREFIX + EMAIL_PREFIX + email;
    return Boolean.TRUE.equals(redisTemplate.hasKey(emailKey));
  }

  public String getJwtKey(String token) {
    String key = RESET_PASSWORD_KEY_PREFIX + token;
    return redisTemplate.opsForValue().get(key);
  }

  public String getTokenFromEmail(String email) {
    String emailKey =  RESET_PASSWORD_KEY_PREFIX + EMAIL_PREFIX + email;
    return valueOperations.get(emailKey);
  }

  public void blacklist(String email, String token) {
    String key = RESET_PASSWORD_KEY_PREFIX + token;
    if (getJwtKey(token) == null) {
      throw new TokenDoesNotExistException("No token found for key " + key);
    }
    String blacklistKey = RESET_PASSWORD_KEY_PREFIX + BLACKLIST_PREFIX + token;
    String emailKey = RESET_PASSWORD_KEY_PREFIX + EMAIL_PREFIX + BLACKLIST_PREFIX + email;
    Long TTL = redisTemplate.getExpire(key, TimeUnit.SECONDS);
    redisTemplate.delete(emailKey);
    redisTemplate.delete(key);
    valueOperations.set(blacklistKey, "true", TTL, TimeUnit.SECONDS);
  }

  public boolean isValid(String email, String token) {
    String blacklistKey = RESET_PASSWORD_KEY_PREFIX + BLACKLIST_PREFIX + token;
    String blacklisted = valueOperations.get(blacklistKey);
    String randomToken = getTokenFromEmail(email);
    return blacklisted == null && randomToken.equals(token);
  }

}
