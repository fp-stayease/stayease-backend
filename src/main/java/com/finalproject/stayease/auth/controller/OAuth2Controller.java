package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.OAuth2RegisterRequestDTO;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.impl.GoogleVerifierService;
import com.finalproject.stayease.auth.service.impl.OneTimeCodeService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}