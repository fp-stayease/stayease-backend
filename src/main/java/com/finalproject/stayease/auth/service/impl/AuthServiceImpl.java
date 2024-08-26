package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.LoginResponseDTO;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UsersService usersService;

  @Override
  public ResponseEntity<?> login(LoginRequestDTO loginRequestDTO) {
    try {
      // * 1: get user details from authentication and security context
      Authentication authentication = authenticateUser(loginRequestDTO);

      // ! 2: generate token
      String accessToken = jwtService.generateAccessToken(authentication);
      String refreshToken = jwtService.generateRefreshToken(authentication.getName());

      // * 3: generate response, set headers(cookie)
      return buildLoginResponse(accessToken, refreshToken);
    } catch (AuthenticationException ex) {
      return handleAuthenticationException(ex);
    }
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


  private ResponseEntity<?> buildLoginResponse(String accessToken, String refreshToken) {
    // * response body
    LoginResponseDTO responseBody = new LoginResponseDTO("Login successful, welcome to StayEase!", accessToken, refreshToken);

    // * set cookie / headers
    HttpHeaders responseHeaders = setHeadersCookie(refreshToken);

    return ResponseEntity.ok().headers(responseHeaders).body(responseBody);
  }

  private HttpHeaders setHeadersCookie(String refreshToken) {
    // * generate cookie
    ResponseCookie cookie = setCookie(refreshToken);

    // * build header
    HttpHeaders headers = new HttpHeaders();
    headers.add("Set-Cookie", cookie.toString());
    return headers;
  }

  private ResponseCookie setCookie(String refreshToken) {
    return ResponseCookie.from("refresh_token", refreshToken)
        .path("/")
        .httpOnly(true)
        .secure(true)
        .maxAge(7 * 24 * 60 * 60)
        .build();
  }

  private ResponseEntity<?> handleAuthenticationException(AuthenticationException ex) {
    if (ex instanceof BadCredentialsException) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Authentication failed. Invalid username or password.");
    } else if (ex instanceof LockedException) {
      return ResponseEntity.status(HttpStatus.LOCKED).body("Account is locked.");
    } else {
      log.error("Authentication failed due to an internal error", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
    }
  }

  // Region - refresh token

  @Override
  public LoginResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse response) throws RuntimeException {
    String refreshToken = jwtService.extractRefreshTokenFromCookie(request);

    if (refreshToken == null) {
      log.warn("(AuthServiceImpl.refreshToken) refreshToken is null");
      throw new BadCredentialsException("Refresh token not found");
    }


    try {
      String email = jwtService.decodeToken(refreshToken).getSubject();

      if (jwtService.isRefreshTokenValid(email, refreshToken)) {
        jwtService.invalidateToken(email);

        String newAccessToken = jwtService.generateAccessTokenFromEmail(email);
        String newRefreshToken = jwtService.generateRefreshToken(email);
        updateRefreshTokenCookie(response, newRefreshToken);

        log.info("(AuthServiceImpl.refreshToken) Tokens refreshed successfully for user: {}", email);
        return new LoginResponseDTO("Access token successfully refreshed!", newAccessToken, newRefreshToken);
      } else {
        log.warn("(AuthServiceImpl.refreshToken) Invalid refresh token for user: {}", email);
        throw new BadCredentialsException("Invalid refresh token");
      }
    } catch (Exception e) {
      log.error("(AuthServiceImpl.refreshToken) Error processing refresh token: " + e.getClass() + ": " + e.getLocalizedMessage());
      // TODO create TokenRefreshFailedException
      throw new RuntimeException("Could not refresh token: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
  }

  private void updateRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie newCookie = setCookie(refreshToken);
    response.setHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
  }

  // Region

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String email = jwtService.extractSubjectFromCookie(request);
    jwtService.invalidateToken(email);
    invalidateSessionAndCookie(request, response);
  }

  private void invalidateSessionAndCookie(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
  }
}
