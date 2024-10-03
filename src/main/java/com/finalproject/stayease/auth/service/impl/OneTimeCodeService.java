package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.repository.OtcRedisRepository;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@Data
public class OneTimeCodeService {

  private final Map<String, UserTokenPair> codeStore = new ConcurrentHashMap<>();
  private static final Long CODE_EXPIRY_MS = 5 * 60 * 1000L;

  private final OtcRedisRepository otcRedisRepository;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final UsersService usersService;

  public String generateAndStoreCode(Users user, String accessToken, String refreshToken) {
    String code = generateUniqueCode();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(Instant.now())
        .subject(user.getEmail())
        .claim("accessToken", accessToken)
        .claim("refreshToken", refreshToken)
        .expiresAt(Instant.now().plus(CODE_EXPIRY_MS, java.time.temporal.ChronoUnit.MILLIS))
        .build();
    var jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    otcRedisRepository.saveJwtKey(code, jwt);
    return code;
  }

  public UserTokenPair getAndRemoveTokens(String code) {
    log.info("getting token from code: " + code);
    String jwt = otcRedisRepository.getJwtKeyAndDelete(code);
    if (jwt == null) {
      throw new DataNotFoundException("Code not found");
    }
    String email = jwtDecoder.decode(jwt).getSubject();
    String accessToken = jwtDecoder.decode(jwt).getClaim("accessToken");
    String refreshToken = jwtDecoder.decode(jwt).getClaim("refreshToken");
    return new UserTokenPair(usersService.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found")), accessToken, refreshToken);
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
  }
}
