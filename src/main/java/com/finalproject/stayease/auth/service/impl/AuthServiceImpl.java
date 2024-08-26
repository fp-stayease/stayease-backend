package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.LoginResponseDTO;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.service.UserService;
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
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserService userService;

  @Override
  public ResponseEntity<?> login(LoginRequestDTO loginRequestDTO) {
    try {
      // * 1: get user details from authentication and security context
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
      );

      if (authentication == null) {
        log.error("User object is null after authentication");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Authentication failed: user object is null");
      }

      // ! 2: generate token
      String accessToken = jwtService.generateAccessToken(authentication);
      String refreshToken = jwtService.generateRefreshToken(authentication.getName());

      // * 3: generate response, set headers(cookie)
      return buildLoginResponse(accessToken, refreshToken);

    } catch (BadCredentialsException ex) {
      // Handle bad credentials
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Authentication failed. Invalid username or password.");
    } catch (LockedException ex) {
      // Handle locked account
      return ResponseEntity.status(HttpStatus.LOCKED).body("Account is locked.");
    } catch (Exception ex) {
      // Handle other exceptions
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
    }
  }


  private ResponseEntity<?> buildLoginResponse(String accessToken, String refreshToken) {
    // * response body
    String message = "Login successful, welcome to StayEase!";
    LoginResponseDTO responseBody = responseBody(message, accessToken, refreshToken);

    // * set cookie / headers
    HttpHeaders responseHeaders = setHeadersCookie(refreshToken);

    return ResponseEntity.ok().headers(responseHeaders).body(responseBody);
  }

  private LoginResponseDTO responseBody(String message, String accessToken, String refreshToken) {
    LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
    loginResponseDTO.setMessage(message);
    loginResponseDTO.setAccessToken(accessToken);
    loginResponseDTO.setRefreshToken(refreshToken);
    return loginResponseDTO;
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

  // Region - refresh token

  @Override
  public LoginResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse response) throws RuntimeException {
    String refreshToken = extractRefreshToken(request);

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

        String message = "Access token successfully refreshed!";
        return responseBody(message, newAccessToken, newRefreshToken);
      } else {
        log.warn("(AuthServiceImpl.refreshToken) Invalid refresh token for user: {}", email);
        throw new BadCredentialsException("Invalid refresh token");
      }
    } catch (Exception e) {
      log.error("(AuthServiceImpl.refreshToken) Error processing refresh token: " + e.getClass() + ": " + e.getLocalizedMessage());
      // TODO create InvalidTokenException
      throw new RuntimeException("Error processing refresh token: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
  }

  private String extractRefreshToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("refresh_token")) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private void updateRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie newCookie = setCookie(refreshToken);
    response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
  }

  // Region

  @Override
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    // * Get logged in user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication is null, no user is currently logged"
                                                                 + " in");
    }
    String email = authentication.getName();
    String token = jwtService.getToken(email);

    if (token != null) {
      // * Invalidate token
      jwtService.invalidateToken(email);
    }

    invalidateSessionAndCookie(request, response);
    return ResponseEntity.ok().body("Logged out successfully!");
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
