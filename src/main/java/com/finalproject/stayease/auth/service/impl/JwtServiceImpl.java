package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.repository.AuthRedisRepository;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
public class JwtServiceImpl implements JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final AuthRedisRepository authRedisRepository;
  private final UserService userService;
  private final UserDetailsServiceImpl userDetailsService;

  public JwtServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, AuthRedisRepository authRedisRepository,
      UserService userService, UserDetailsServiceImpl userDetailsService) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.authRedisRepository = authRedisRepository;
    this.userService = userService;
    this.userDetailsService = userDetailsService;
  }

  private static final ChronoUnit ACCESS_TOKEN_TIME_UNIT = ChronoUnit.MINUTES;

  @Override
  public String generateAccessToken(Authentication authentication) {
    Instant now = Instant.now();

    List<String> authorities = authentication.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException(
        "User not found"));

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(1, ACCESS_TOKEN_TIME_UNIT))
        .subject(authentication.getName())
        .claim("userId", user.getId())
        .claim("userType", user.getUserType())
        .claim("authorities", authorities)
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  @Override
  public String generateAccessTokenFromEmail(String email) {
    Instant now = Instant.now();

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    List<String> authorities = userDetails.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    User user = userService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
        "User not found"));

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(1, ACCESS_TOKEN_TIME_UNIT))
        .subject(userDetails.getUsername())
        .claim("userId", user.getId())
        .claim("userType", user.getUserType())
        .claim("authorities", authorities)
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  @Override
  public String generateRefreshToken(String email) {
    Instant now = Instant.now();

    User user = userService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
        "User not found"));

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(7, ChronoUnit.DAYS))
        .subject(email)
        .claim("userId", user.getId())
        .build();

    String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    // Store the refresh token in Redis
    authRedisRepository.saveJwtKey(user.getEmail(), refreshToken);

    return refreshToken;
  }

  @Override
  public String getToken(String email) {
    return authRedisRepository.getJwtKey(email);
  }

  @Override
  public String extractSubjectFromToken(HttpServletRequest request, String token) {
    try {
      Jwt jwt = decodeToken(token);
      return jwt.getSubject();
    } catch (JwtException e) {
      if (isTokenExpiredThrowsException(token)) {
        throw new JwtException("JWT token is expired");
      }
      throw e;
    } catch (Exception e) {
      log.error("(JwtServiceImp.extractSubjectFromToken) Error extracting username from token", e);
      return null;
    }
  }

  private boolean isTokenExpiredThrowsException(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return isTokenExpired(jwt);
    } catch (JwtException e) {
      if (e.getMessage().contains("Jwt expired at")) {
        log.info("Expired JWT token");
        return true;
      } else {
        log.error("(JwtServiceImpl.isTokenExpired) Invalid token", e);
      }
      // Handle other JWT exceptions
      throw e;
    }
  }

  @Override
  public String extractSubjectFromCookie(HttpServletRequest request) {
    String refreshToken = extractRefreshTokenFromCookie(request);
    if (refreshToken != null) {
      return extractSubjectFromToken(request, refreshToken);
    }
    return null;
  }

  @Override
  public String extractRefreshTokenFromCookie(HttpServletRequest request) throws RuntimeException {
    return Arrays.stream(Optional.ofNullable(request.getCookies())
            .orElseThrow(() -> new BadCredentialsException("No cookies present")))
        .filter(cookie -> "refresh_token".equals(cookie.getName()))
        .findFirst()
        .map(Cookie::getValue)
        .orElse(null);
  }

  @Override
  public void invalidateToken(String email) {
    authRedisRepository.blacklistKey(email);
  }

  @Override
  public boolean isRefreshTokenValid(String email, String refreshToken) {
    return authRedisRepository.isValid(email, refreshToken);
  }

  @Override
  public boolean isAccessTokenValid(String accessToken, String email) {
    try {
      Jwt jwt = decodeToken(accessToken);
      String tokenEmail = jwt.getSubject();
      return (tokenEmail != null && tokenEmail.equals(email) && !isTokenExpired(jwt));
    } catch (JwtException e) {
      // Token is invalid or expired
      return false;
    }
  }

  private boolean isTokenExpired(Jwt jwt) {
    return jwt.getExpiresAt().isBefore(Instant.now());
  }

  @Override
  public Jwt decodeToken(String token) {
    try {
      return jwtDecoder.decode(token);
    } catch (JwtException e) {
      // Handle invalid token
      throw new JwtException("Invalid JWT token: " + e.getLocalizedMessage());
    }
  }

  @Override
  public Authentication getAuthenticationFromToken(String token) {
    Jwt jwt = decodeToken(token);

    Collection<GrantedAuthority> authorities = jwt.getClaimAsStringList("authorities")
        .stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    User principal = new User();
    principal.setId(jwt.getClaim("userId"));
    principal.setEmail(jwt.getSubject());
    principal.setUserType(UserType.valueOf(jwt.getClaim("userType")));

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }
}
