package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Data
@Transactional
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final UsersService usersService;
  private final OneTimeCodeService oneTimeCodeService;


  @Value("${REFRESH_TOKEN_EXPIRY_IN_SECONDS:604800}")
  private int REFRESH_TOKEN_EXPIRY_IN_SECONDS;
  @Value("${FE_URL}")
  private String FE_URL;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SELECT_USER_TYPE"))) {
      redirectToSelectUserType(response);
    }

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    Optional<Users> existingUser = usersService.findByEmail(email);

    if (existingUser.isPresent()) {
      generateTokensAndResponse(response, authentication, existingUser.get());
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("User not found");
    }

  }

  private void redirectToSelectUserType(HttpServletResponse response) throws IOException {
    String redirectUrl = UriComponentsBuilder.fromUriString(FE_URL)
        .path("/register/select-user-type")
        .build().toUriString();
    response.sendRedirect(redirectUrl);
  }

  private void generateTokensAndResponse(HttpServletResponse response, Authentication authentication, Users user) throws IOException {
    String accessToken = jwtService.generateAccessToken(authentication);
    String refreshToken = jwtService.generateRefreshToken(authentication.getName());

    ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
        .path("/")
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .maxAge(REFRESH_TOKEN_EXPIRY_IN_SECONDS)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    sendTokenResponse(response, user, accessToken, refreshToken);
  }

  private void sendTokenResponse(HttpServletResponse response, Users user, String accessToken, String refreshToken) throws IOException {
//    Map<String, String> tokens = new HashMap<>();
//    tokens.put("message:", "Successfully logged in using socials login!");
//    tokens.put("access_token", accessToken);
//    tokens.put("refresh_token", generateTokenFromEmail);
//
//    response.setContentType("application/json");
//    response.getWriter().write(new ObjectMapper().writeValueAsString(tokens));

    String oneTimeCode = oneTimeCodeService.generateAndStoreCode(user, accessToken, refreshToken);
    String redirectUrl = UriComponentsBuilder.fromUriString(FE_URL)
        .path("/callback")
        .queryParam("code", oneTimeCode)
        .build().toUriString();

    response.sendRedirect(redirectUrl);
  }

}
