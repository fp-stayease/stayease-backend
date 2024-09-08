package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.CodeExchangeRequestDTO;
import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.model.dto.SocialSelectUserTypeDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.impl.OneTimeCodeService;
import com.finalproject.stayease.auth.service.impl.OneTimeCodeService.UserTokenPair;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.service.SocialLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/oauth2")
@Slf4j
@Data
public class OAuth2Controller {

  private final UserDetailsServiceImpl userDetailsService;
  private final OneTimeCodeService oneTimeCodeService;
  private final SocialLoginService socialLoginService;
  private final AuthService authService;
  private final HttpSession session;

  @Value("${REFRESH_TOKEN_EXPIRY_IN_SECONDS:604800}")
  private int REFRESH_TOKEN_EXPIRY_IN_SECONDS;

  @PostMapping("/exchange-code")
  public ResponseEntity<Response<AuthResponseDto>> exchangeCode(@RequestBody CodeExchangeRequestDTO requestDTO) {
    UserTokenPair userTokenPair = oneTimeCodeService.getAndRemoveTokens(requestDTO.getCode());
    AuthResponseDto responseDto = new AuthResponseDto(userTokenPair.getUser(), new TokenResponseDto(userTokenPair.getAccessToken(), userTokenPair.getRefreshToken()));
    return Response.successfulResponse(200, "OAuth2 sign in successful!", responseDto);
  }

  @PostMapping("/user-select")
  public ResponseEntity<Response<AuthResponseDto>> selectUserType(HttpServletResponse response, HttpServletRequest request, @RequestBody SocialSelectUserTypeDTO requestDTO) {
    UserType userType = requestDTO.getUserType();
    Map<String, Object> oAuth2UserInfo = getOAuth2UserInfo();
    log.info("user info:" + oAuth2UserInfo);

    String email = (String) oAuth2UserInfo.get("email");

    Users newUser = registerOAuth2User(requestDTO, oAuth2UserInfo);
    TokenResponseDto tokenResponseDto = authService.generateTokenFromEmail(email);
    addRefreshTokenCookie(response, tokenResponseDto);
    authenticateUser(request, email);
    AuthResponseDto responseDto = new AuthResponseDto(newUser, tokenResponseDto);

    clearOAuth2SessionData();

    return Response.successfulResponse(HttpStatus.ACCEPTED.value(), "Successfully set user type!", responseDto);
  }

  private Map<String, Object> getOAuth2UserInfo() {
    Map<String, Object> oAuth2UserInfo = (Map<String, Object>) session.getAttribute("oAuth2UserInfo");
    if (oAuth2UserInfo == null) {
      throw new DataNotFoundException("No userinfo found");
    }
    return oAuth2UserInfo;
  }

  private Users registerOAuth2User(SocialSelectUserTypeDTO requestDTO, Map<String, Object> oAuth2UserInfo) {
    String provider = (String) session.getAttribute("provider");
    String providerUserId = (String) session.getAttribute("providerUserId");
    String email = (String) oAuth2UserInfo.get("email");
    String firstName = (String) oAuth2UserInfo.get("given_name");
    String lastName = (String) oAuth2UserInfo.get("family_name");
    String pictureUrl = (String) oAuth2UserInfo.get("picture");

    SocialLoginRequest requestDto = new SocialLoginRequest(provider, providerUserId, email, requestDTO.getUserType(), firstName, lastName, pictureUrl);
    return socialLoginService.registerOAuth2User(requestDto);
  }

  private void addRefreshTokenCookie(HttpServletResponse response, TokenResponseDto tokenResponseDto) {
    Cookie cookie = new Cookie("refresh_token", tokenResponseDto.getRefreshToken());
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(REFRESH_TOKEN_EXPIRY_IN_SECONDS);
    response.addCookie(cookie);
    response.setHeader("Authorization", "Bearer " + tokenResponseDto.getAccessToken());
  }

  private void authenticateUser(HttpServletRequest request, String email) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void clearOAuth2SessionData() {
    session.removeAttribute("oAuth2UserInfo");
    session.removeAttribute("provider");
    session.removeAttribute("providerId");
  }
}