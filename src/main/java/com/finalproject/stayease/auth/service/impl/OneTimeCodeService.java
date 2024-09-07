package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class OneTimeCodeService {

  private final Map<String, UserTokenPair> codeStore = new ConcurrentHashMap<>();
  private static final Long CODE_EXPIRY_MS = 5 * 60 * 1000L;

  public String generateAndStoreCode(Users user, String accessToken, String refreshToken) {
    String code = generateUniqueCode();
    UserTokenPair userTokenPair = new UserTokenPair(user, accessToken, refreshToken,
        System.currentTimeMillis() + CODE_EXPIRY_MS);
    codeStore.put(code, userTokenPair);
    log.info("code storage: " + codeStore);
    return code;
  }

  public UserTokenPair getAndRemoveTokens(String code) {
    log.info("getting token from code: " + code);
    UserTokenPair userTokenPair = codeStore.remove(code);
    if (userTokenPair == null) {
      throw new DataNotFoundException("Code not found");
    } if (userTokenPair.expiryTime < System.currentTimeMillis()) {
      throw new InvalidRequestException("Code is expired!");
    } else {
      return userTokenPair;
    }
  }

  private String generateUniqueCode() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  @Data
  @AllArgsConstructor
  public static class UserTokenPair {

    private Users user;
    String accessToken;
    String refreshToken;
    long expiryTime;
  }
}
