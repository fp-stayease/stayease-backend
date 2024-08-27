package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.LoginResponseDTO;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.service.RegisterService;
import com.finalproject.stayease.users.service.SocialLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final AuthService authService;
  private final JwtService jwtService;

  private final static int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

  @GetMapping("")
  public String getLoggedInUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    String role = auth.getAuthorities().iterator().next().getAuthority();
    if (username.equals("anonymousUser")) {
      return "Not logged in";
    }
    return "Logged in user: " + username + " with role: " + role;
  }

  @PostMapping("/register")
  public ResponseEntity<Response<InitialRegistrationResponseDTO>> initiateUserRegistration(@RequestParam("userType") String type, @Valid @RequestBody InitialRegistrationRequestDTO requestDTO) {
    UserType userType = UserType.valueOf(type.toUpperCase());
    return Response.successfulResponse(HttpStatus.OK.value(), "Initial 'User' registration successful!",
        registerService.initialRegistration(requestDTO, userType));
  }

  @PostMapping("/register/verify")
  public ResponseEntity<Response<VerifyUserResponseDTO>> verifyRegistration(@RequestParam String token,
      @Valid @RequestBody VerifyRegistrationDTO verifyRegistrationDTO) {
    return Response.successfulResponse(HttpStatus.ACCEPTED.value(), "Verification successful, welcome to StayEase!", registerService.verifyRegistration(verifyRegistrationDTO, token));
  }

  @PostMapping("/register/socials/user-select")
  public ResponseEntity<Response<Object>> selectUserType(@RequestBody UserType userType) {
    socialLoginService.changeUserType(userType);
    return Response.successfulResponse(HttpStatus.ACCEPTED.value(), "Successfully set user type!", null);
  }

  @PostMapping("/login")
  public ResponseEntity<Response<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletResponse response) {
    LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
    addRefreshTokenCookie(response, loginResponseDTO.getRefreshToken());
    return Response.successfulResponse(HttpStatus.OK.value(), "Successfully logged in!", loginResponseDTO);
  }

  @PostMapping("/logout")
  public ResponseEntity<Response<String>> logout(HttpServletRequest request, HttpServletResponse response) {
    String email = jwtService.extractSubjectFromToken(extractAccessToken(request));
    authService.logout(email);
    invalidateSessionAndCookie(request, response);
    return Response.successfulResponse("Logged out successfully!");
  }

  private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie("refresh_token", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(COOKIE_MAX_AGE);
    response.addCookie(cookie);
  }

  private String extractAccessToken(HttpServletRequest request) {
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
