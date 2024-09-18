package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.CodeExchangeRequestDTO;
import com.finalproject.stayease.auth.model.dto.OAuth2RegisterRequestDTO;
import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.model.dto.SocialSelectUserTypeDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.impl.GoogleVerifierService;
import com.finalproject.stayease.auth.service.impl.OneTimeCodeService;
import com.finalproject.stayease.auth.service.impl.OneTimeCodeService.UserTokenPair;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.entity.dto.UsersProfileDTO;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
  private final GoogleVerifierService googleVerifierService;
  private final UsersService usersService;
  private final SocialLoginService socialLoginService;
  private final AuthService authService;
  private final HttpSession session;

  @Value("${token.expiration.refresh:2592000}")
  private int REFRESH_TOKEN_EXPIRY_IN_SECONDS;

  @PostMapping("/check-user-exists")
  public ResponseEntity<Response<Boolean>> checkUserExists(@RequestBody String email) {
    boolean exists = false;
    String decodedEmail = URLDecoder.decode(email.trim().replaceAll("=+$", ""), StandardCharsets.UTF_8);
    log.info("Checking if user exists: " + decodedEmail);
    Optional<Users> user = usersService.findByEmail(decodedEmail);
    if (user.isPresent()) {
      exists = true;
      log.info("User exists: " + user.get().getEmail());
    }
    log.info("User exists: " + exists);
    return Response.successfulResponse(200, "User exists", exists);
  }

  @PostMapping("/exchange-code")
  public ResponseEntity<Response<AuthResponseDto>> exchangeCode(@RequestBody String googleToken) {
    AuthResponseDto responseDto = googleVerifierService.exchangeGoogleToken(googleToken);
    return Response.successfulResponse(200, "OAuth2 sign in successful!", responseDto);
  }

  @PostMapping("/register")
  public ResponseEntity<Response<AuthResponseDto>> registerOAuth2User(
      @RequestBody OAuth2RegisterRequestDTO requestDTO) {
    return Response.successfulResponse(200, "Successfully registered user!",
        googleVerifierService.registerOAuth2User(requestDTO));
  }

//  private void addRefreshTokenCookie(HttpServletResponse response, TokenResponseDto tokenResponseDto) {
//    Cookie cookie = new Cookie("refresh_token", tokenResponseDto.getRefreshToken());
//    cookie.setHttpOnly(true);
//    cookie.setSecure(true);
//    cookie.setPath("/");
//    cookie.setMaxAge(REFRESH_TOKEN_EXPIRY_IN_SECONDS);
//    response.addCookie(cookie);
//    response.setHeader("Authorization", "Bearer " + tokenResponseDto.getAccessToken());
//  }

  // @PostMapping("/exchange-code")
//  public ResponseEntity<Response<AuthResponseDto>> exchangeCode(@RequestBody CodeExchangeRequestDTO requestDTO) {
//    UserTokenPair userTokenPair = oneTimeCodeService.getAndRemoveTokens(requestDTO.getCode());
//    AuthResponseDto responseDto = new AuthResponseDto(userTokenPair.getUser(), new TokenResponseDto(userTokenPair.getAccessToken(), userTokenPair.getRefreshToken()));
//    return Response.successfulResponse(200, "OAuth2 sign in successful!", responseDto);
//  }
}