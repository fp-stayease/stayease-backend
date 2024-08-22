package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.entity.UserAuth;
import com.finalproject.stayease.auth.repository.AuthRedisRepository;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final AuthRedisRepository authRedisRepository;

  public JwtServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, AuthRedisRepository authRedisRepository) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.authRedisRepository = authRedisRepository;
  }

  @Override
  public String generateToken(User user) {
    Instant now = Instant.now();

    UserAuth userAuth = new UserAuth(user);

    List<String> authorities = userAuth.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(12, ChronoUnit.HOURS))
        .subject(user.getEmail())
        .claim("userId", user.getId())
        .claim("userType", user.getUserType())
        .claim("authorities", authorities)
        .build();

    String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    authRedisRepository.saveJwtKey(user.getEmail(), jwt);

    return jwt;
  }

  @Override
  public void invalidateToken(String email) {
    authRedisRepository.blacklistKey(email);
  }

  @Override
  public boolean isTokenValid(String token, String email) {
    return authRedisRepository.isValid(token, email);
  }

  @Override
  public Jwt decodeToken(String token) {
    return jwtDecoder.decode(token);
  }

  @Override
  public Authentication getAuthentication(String token) {
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
