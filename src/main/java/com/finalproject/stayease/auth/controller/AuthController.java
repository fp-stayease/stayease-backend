package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.ResetPasswordService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import com.finalproject.stayease.exceptions.TokenDoesNotExistException;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.service.RegisterService;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

  private final RegisterService registerService;
  private final SocialLoginService socialLoginService;
  private final UserDetailsServiceImpl userDetailsService;
  private final AuthService authService;
  private final ResetPasswordService resetPasswordService;
  private final UsersService usersService;
  private final JwtService jwtService;

  @Value("${REFRESH_TOKEN_EXPIRY_IN_SECONDS:604800}")
  private int REFRESH_TOKEN_EXPIRY_IN_SECONDS;

  @GetMapping("/status")
  public ResponseEntity<Response<AuthResponseDto>> getLoggedInUser(HttpServletRequest request) {
    try {
      Users loggedInUser = usersService.getLoggedUser();
      TokenResponseDto tokenResponseDto = new TokenResponseDto(extractTokenFromRequest(request),
          extractRefreshToken(request));
      if (loggedInUser.getEmail().equals("anonymousUser")) {
        return Response.failedResponse(401, "No logged in user");
      }
      AuthResponseDto response = new AuthResponseDto(loggedInUser, tokenResponseDto);
      return Response.successfulResponse(HttpStatus.OK.value(), "Displaying auth status", response);
    } catch (AccessDeniedException e) {
      return Response.failedResponse(401, "No user logged in");
    }
  }

  @PostMapping("/register")
  public ResponseEntity<Response<InitialRegistrationResponseDTO>> initiateUserRegistration(
      @RequestParam("userType") String type, @Valid @RequestBody InitialRegistrationRequestDTO requestDTO)
      throws MessagingException, IOException {
    UserType userType = UserType.valueOf(type.toUpperCase());
    return Response.successfulResponse(HttpStatus.OK.value(), "Initial " + userType + " registration successful!",
        registerService.initialRegistration(requestDTO, userType));
  }

  @PostMapping("/register/verify")
  public ResponseEntity<Response<VerifyUserResponseDTO>> verifyRegistration(@RequestParam String token,
      @Valid @RequestBody VerifyRegistrationDTO verifyRegistrationDTO) {
    return Response.successfulResponse(HttpStatus.ACCEPTED.value(), "Verification successful, welcome to StayEase!",
        registerService.verifyRegistration(verifyRegistrationDTO, token));
  }

  @PostMapping("/register/socials/user-select")
  public ResponseEntity<Response<Object>> selectUserType(@RequestBody String role) {
    UserType userType = UserType.valueOf(role.toUpperCase());
    socialLoginService.changeUserType(userType);
    return Response.successfulResponse(HttpStatus.ACCEPTED.value(), "Successfully set user type!", null);
  }

  @PostMapping("/login")
  public ResponseEntity<Response<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletResponse response) {
    TokenResponseDto tokenResponseDto = authService.login(loginRequestDTO);
    addRefreshTokenCookie(response, tokenResponseDto);
    Users loggedInUser = usersService.getLoggedUser();
    AuthResponseDto authResponseDto = new AuthResponseDto(loggedInUser, tokenResponseDto);
    return Response.successfulResponse(HttpStatus.OK.value(), "Successfully logged in!", authResponseDto);
  }

  @PostMapping("/logout")
  public ResponseEntity<Response<String>> logout(HttpServletRequest request, HttpServletResponse response) {
    String email = jwtService.extractSubjectFromToken(extractRefreshToken(request));
    authService.logout(email);
    invalidateSessionAndCookie(request, response);
    return Response.successfulResponse("Logged out successfully!");
  }

  @PostMapping("/refresh")
  public ResponseEntity<Response<TokenResponseDto>> refreshToken(@CookieValue(name = "refresh_token", required =
      false) String refreshToken, HttpServletRequest request, HttpServletResponse response) {
    if (refreshToken == null) {
      throw new TokenDoesNotExistException("No refresh token found!");
    }
    String email = jwtService.extractSubjectFromToken(refreshToken);
    if (jwtService.isRefreshTokenValid(refreshToken, email)) {
      TokenResponseDto tokenResponseDto = authService.refreshToken(email);
      addRefreshTokenCookie(response, tokenResponseDto);
      authenticateUser(request, email);
      return Response.successfulResponse(HttpStatus.OK.value(), "Successfully refreshed token!", tokenResponseDto);
    } else {
      return Response.failedResponse(401, "Invalid refresh token!");
    }
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Response<ForgotPasswordResponseDTO>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException {
    return Response.successfulResponse(HttpStatus.OK.value(), "Reset password requested!",
        resetPasswordService.requestResetToken(requestDTO));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Response<Object>> resetPassword(@RequestParam String token, @Valid @RequestBody
      ResetPasswordRequestDTO requestDTO) {
    resetPasswordService.resetPassword(token, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Password successfully reset!", null);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private void authenticateUser(HttpServletRequest request, final String email) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
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

  private String extractRefreshToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      return Arrays.stream(cookies)
          .filter(cookie -> "refresh_token".equals(cookie.getName()))
          .findFirst()
          .map(Cookie::getValue)
          .orElse(null);
    }
    return null;
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
