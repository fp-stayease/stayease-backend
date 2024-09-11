package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final UsersService usersService;

  @Override
  public TokenResponseDto login(LoginRequestDTO loginRequestDTO) {
    // * 1: get user details from authentication and security context
    Authentication authentication = authenticateUser(loginRequestDTO);

    // ! 2: generate token
    String accessToken = jwtService.generateAccessToken(authentication);
    String refreshToken = jwtService.generateRefreshToken(authentication.getName());

    // * 3: generate response, set headers(cookie)
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return new TokenResponseDto(accessToken, refreshToken);
  }

  private Authentication authenticateUser(LoginRequestDTO loginRequestDTO) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
    );
    if (authentication == null) {
      throw new InternalAuthenticationServiceException("Authentication failed: user object is null");
    }
    return authentication;
  }

  @Override
  public void logout(String email) {
    jwtService.invalidateToken(email);
  }

  @Override
  public TokenResponseDto generateTokenFromEmail(String email) {
    String newAccessToken = jwtService.generateAccessTokenFromEmail(email);
    String newRefreshToken = jwtService.generateRefreshToken(email);
    return new TokenResponseDto(newAccessToken, newRefreshToken);
  }
}
