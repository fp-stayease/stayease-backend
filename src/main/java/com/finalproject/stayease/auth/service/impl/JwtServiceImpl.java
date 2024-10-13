package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.repository.AuthRedisRepository;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
public class JwtServiceImpl implements JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final AuthRedisRepository authRedisRepository;
  private final UsersService usersService;
  private final UserDetailsServiceImpl userDetailsService;

  public JwtServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, AuthRedisRepository authRedisRepository,
      UsersService usersService, UserDetailsServiceImpl userDetailsService) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.authRedisRepository = authRedisRepository;
    this.usersService = usersService;
    this.userDetailsService = userDetailsService;
  }

  @Value("${token.expiration.access:3600}")
  private int ACCESS_TOKEN_EXPIRY_IN_SECONDS;
  @Value("${token.expiration.refresh:2592000}")
  private int REFRESH_TOKEN_EXPIRY_IN_SECONDS;

  @Override
  public String generateAccessToken(Authentication authentication) {
    List<String> authorities = authentication.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    Users user = usersService.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException(
        "User not found"));

    JwtClaimsSet claimsSet = buildAccessTokenClaimsSet(user, authorities, user.getEmail());

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  @Override
  public String generateAccessTokenFromEmail(String email) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    List<String> authorities = userDetails.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    Users user = usersService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
        "User not found"));

    JwtClaimsSet claimsSet = buildAccessTokenClaimsSet(user, authorities, userDetails.getUsername());

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  @Override
  public Long getExpiresAt(String token) {
    return Objects.requireNonNull(jwtDecoder.decode(token).getExpiresAt()).toEpochMilli();
  }

  private JwtClaimsSet buildAccessTokenClaimsSet(Users user, List<String> authorities, String subject) {
    Instant now = Instant.now();
    return JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(ACCESS_TOKEN_EXPIRY_IN_SECONDS, ChronoUnit.SECONDS))
        .subject(subject)
        .claim("userId", user.getId())
        .claim("userType", user.getUserType())
        .claim("authorities", authorities)
        .build();
  }

  @Override
  public String generateRefreshToken(String email) {
    Instant now = Instant.now();

    Users user = usersService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
        "User not found"));

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(REFRESH_TOKEN_EXPIRY_IN_SECONDS, ChronoUnit.SECONDS))
        .subject(email)
        .claim("userId", user.getId())
        .build();

    log.info("value refresh expiry: " + claimsSet.getExpiresAt());

    String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    // Store the refresh token in Redis
    authRedisRepository.blacklistKey(user.getEmail());
    authRedisRepository.saveJwtKey(user.getEmail(), refreshToken);

    return refreshToken;
  }

  @Override
  public String extractSubjectFromToken(String token) {
    return decodeToken(token).getSubject();
  }

  @Override
  public Map<String, Object> extractClaimsFromToken(String token) { return decodeToken(token).getClaims(); }

  @Override
  public void invalidateToken(String email) {
    authRedisRepository.blacklistKey(email);
  }

  @Override
  public boolean isRefreshTokenValid(String refreshToken, String email) {
    return authRedisRepository.isValid(refreshToken, email);
  }

  @Override
  public boolean isAccessTokenValid(String token, String email) {
    try {
      Jwt jwt = decodeToken(token);
      String tokenEmail = jwt.getSubject();
      return tokenEmail.equals(email) && !isTokenExpired(jwt);
    } catch (Exception e) {
      log.error("(JwtServiceImpl.isAccessTokenValid:205)Token validation failed: " + e.getClass() + ": "
                + e.getLocalizedMessage());
      return false;
    }
  }

  private boolean isTokenExpired(Jwt jwt) {
    return Objects.requireNonNull(jwt.getExpiresAt()).isBefore(Instant.now());
  }

  @Override
  public Jwt decodeToken(String token) {
    try {
      return jwtDecoder.decode(token);
    } catch (JwtException e) {
      if (e.getMessage().contains("Jwt expired at")) {
        throw new ExpiredJwtException(null, null, "Expired JWT token");
      } else {
        throw e;
      }
    }
  }
}
