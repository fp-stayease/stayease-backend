package com.finalproject.stayease.auth.controller;

import static com.finalproject.stayease.auth.util.SessionCookieUtil.addRefreshTokenCookie;
import static com.finalproject.stayease.auth.util.SessionCookieUtil.extractRefreshToken;
import static com.finalproject.stayease.auth.util.SessionCookieUtil.invalidateSessionAndCookie;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.request.EmailRequestDTO;
import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.request.TokenRequestDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import com.finalproject.stayease.exceptions.utils.InvalidTokenException;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping("api/v1/auth")
@Slf4j
public class AuthController {

  private final UserDetailsServiceImpl userDetailsService;
  private final AuthService authService;
  private final UsersService usersService;
  private final JwtService jwtService;
  private final HttpSession session;


  @GetMapping("/status")
  public ResponseEntity<Response<AuthResponseDto>> getLoggedInUser(HttpServletRequest request) {
    try {
      Users loggedInUser = usersService.getLoggedUser();
      String refreshToken = extractRefreshToken(request);
      Long expiresAt = jwtService.getExpiresAt(refreshToken);
      TokenResponseDto tokenResponseDto = new TokenResponseDto(extractTokenFromRequest(request),
         refreshToken, expiresAt);
      if (loggedInUser.getEmail().equals("anonymousUser")) {
        return Response.failedResponse(401, "No logged in user");
      }
      AuthResponseDto response = new AuthResponseDto(loggedInUser, tokenResponseDto);
      return Response.successfulResponse(HttpStatus.OK.value(), "Displaying auth status", response);
    } catch (AccessDeniedException e) {
      return Response.failedResponse(401, "No user logged in");
    }
  }

  @PostMapping("/login")
  public ResponseEntity<Response<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
    TokenResponseDto tokenResponseDto = authService.login(loginRequestDTO);
    addRefreshTokenCookie(response, tokenResponseDto);
    Users loggedInUser = usersService.getLoggedUser();
    AuthResponseDto authResponseDto = new AuthResponseDto(loggedInUser, tokenResponseDto);
    return Response.successfulResponse(HttpStatus.OK.value(), "Successfully logged in!", authResponseDto);
  }

  @PostMapping("/logout")
  public ResponseEntity<Response<String>> logout(HttpServletRequest request, HttpServletResponse response,
      @RequestBody EmailRequestDTO requestDTO) {
    log.info("Logging out user");
    authService.logout(requestDTO.getEmail());
    invalidateSessionAndCookie(request, response);

    log.info("Logged out successfully!");
    return Response.successfulResponse("Logged out successfully!");
  }

  @PostMapping("/refresh")
  public ResponseEntity<Response<TokenResponseDto>> refreshBothTokens(@RequestBody TokenRequestDTO refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null) {
      throw new InvalidTokenException("No refresh token found!");
    }

    String email = jwtService.extractSubjectFromToken(refreshToken.getToken());
    if (jwtService.isRefreshTokenValid(refreshToken.getToken(), email)) {

      log.info("Refreshing token for email: {}", email);
      TokenResponseDto tokenResponseDto = authService.generateTokenFromEmail(email);
      addRefreshTokenCookie(response, tokenResponseDto);

      log.info("Successfully refreshed token!");
      return Response.successfulResponse(HttpStatus.OK.value(), "Successfully refreshed token!", tokenResponseDto);
    } else {
      return Response.failedResponse(401, "Invalid refresh token!");
    }
  }

  @PostMapping("/refresh-access")
  public ResponseEntity<Response<TokenResponseDto>> refreshAccessToken(@RequestBody TokenRequestDTO refreshToken
  , HttpServletResponse response) {
    String email = jwtService.extractSubjectFromToken(refreshToken.getToken());
    if (jwtService.isRefreshTokenValid(refreshToken.getToken(), email)) {

      log.info("Refreshing access token for email: {}", email);
      TokenResponseDto tokenResponseDto = authService.refreshAccessToken(refreshToken.getToken());
      addRefreshTokenCookie(response, tokenResponseDto);

      log.info("Successfully refreshed access token!");
      return Response.successfulResponse(HttpStatus.OK.value(), "Successfully refreshed access token!",
          tokenResponseDto);
    } else {
      return Response.failedResponse(401, "Invalid refresh token!");
    }
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

}