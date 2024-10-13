package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.OAuth2RegisterRequestDTO;
import com.finalproject.stayease.auth.model.dto.request.EmailRequestDTO;
import com.finalproject.stayease.auth.model.dto.request.TokenRequestDTO;
import com.finalproject.stayease.auth.service.impl.GoogleVerifierService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

  private final GoogleVerifierService googleVerifierService;
  private final UsersService usersService;

  @PostMapping("/check-user-exists")
  public ResponseEntity<Response<Boolean>> checkUserExists(@RequestBody EmailRequestDTO emailRequestDTO) {
    boolean exists = false;
    String email = emailRequestDTO.getEmail();
    String decodedEmail = URLDecoder.decode(email.trim().replaceAll("=+$", ""), StandardCharsets.UTF_8);
    log.info("Checking if user exists: {}", decodedEmail);
    Optional<Users> user = usersService.findByEmail(decodedEmail);
    if (user.isPresent()) {
      exists = true;
    }
    log.info("User exists: {}", exists);
    return Response.successfulResponse(200, "User exists", exists);
  }

  @PostMapping("/exchange-code")
  public ResponseEntity<Response<AuthResponseDto>> exchangeCode(@RequestBody TokenRequestDTO tokenRequestDTO) {
    String googleToken = tokenRequestDTO.getToken();
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