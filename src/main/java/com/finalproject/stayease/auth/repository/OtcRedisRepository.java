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
public class OtcRedisRepository {

  private final static String STRING_KEY_PREFIX = "stayease:oauth2:otc:";
  private final static Integer OTC_EXPIRE_MINUTES = 5;

  private final ValueOperations<String, String> valueOperations;
  private final RedisTemplate<String, String> redisTemplate;

  public OtcRedisRepository(RedisTemplate<String, String> redisTemplate) {
    this.valueOperations = redisTemplate.opsForValue();
    this.redisTemplate = redisTemplate;
  }

  public void saveJwtKey(String code, String JwtKey) {
    String key = STRING_KEY_PREFIX + code;
    redisTemplate.delete(key);
    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      log.info("Previous token deleted for code: {}", code);
    }
    valueOperations.set(STRING_KEY_PREFIX + code, JwtKey, OTC_EXPIRE_MINUTES, TimeUnit.MINUTES);
    log.info("Saved OTC value: {}", JwtKey);
  }

  public String getJwtKeyAndDelete(String code) {
    String jwtKey = valueOperations.get(STRING_KEY_PREFIX + code);
    redisTemplate.delete(STRING_KEY_PREFIX + code);
    return jwtKey;
  }
}
