package com.finalproject.stayease.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Data
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  public final JwtService jwtService;
  public final UsersService usersService;

  @Value("${REFRESH_TOKEN_EXPIRY_IN_SECONDS:604800}")
  private int REFRESH_TOKEN_EXPIRY_IN_SECONDS;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    Optional<Users> existingUser = usersService.findByEmail(email);

    if (existingUser.isPresent()) {
      generateTokensAndResponse(response, authentication);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("User not found");
    }

  }

  private void generateTokensAndResponse(HttpServletResponse response, Authentication authentication) throws IOException {
    String accessToken = jwtService.generateAccessToken(authentication);
    String refreshToken = jwtService.generateRefreshToken(authentication.getName());

    ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
        .path("/")
        .httpOnly(true)
        .secure(true)
        .maxAge(REFRESH_TOKEN_EXPIRY_IN_SECONDS)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    sendTokenResponse(response, accessToken, refreshToken);
  }

  private void sendTokenResponse(HttpServletResponse response, String accessToken, String refreshToken) throws IOException {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("message:", "Successfully logged in using socials login!");
    tokens.put("access_token", accessToken);
    tokens.put("refresh_token", refreshToken);

    response.setContentType("application/json");
    response.getWriter().write(new ObjectMapper().writeValueAsString(tokens));
  }

}
