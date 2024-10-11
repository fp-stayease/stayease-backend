package com.finalproject.stayease.auth.service.helpers;

import com.finalproject.stayease.auth.repository.ResetPasswordRedisRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenGenerator {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final ResetPasswordRedisRepository redisRepository;

  @Value("${token.expire.hours:1}")
  private long TOKEN_EXPIRE;

  /**
   * Generates a random key for password reset.
   */
  public String generateRandomKey(String email) {
    String resetToken = generateResetToken(email);
    String randomKey = UUID.randomUUID().toString();
    redisRepository.saveResetToken(email, randomKey, resetToken);
    return randomKey;
  }

  /**
   * Checks if a given token is valid.
   */
  public boolean isTokenValid(String token) {
    try {
      String jwtToken = redisRepository.getJwtKey(token);
      String email = getEmailFromToken(jwtToken);
      return redisRepository.isValid(email, token);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extracts the email from a JWT token.
   */
  public String getEmailFromToken(String token) {
    return jwtDecoder.decode(token).getSubject();
  }

  private String generateResetToken(String email) {
    String jti = UUID.randomUUID().toString().substring(0, 10).replace("-", "").toUpperCase();
    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(TOKEN_EXPIRE, ChronoUnit.HOURS))
        .id(jti)
        .subject(email)
        .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }
}
