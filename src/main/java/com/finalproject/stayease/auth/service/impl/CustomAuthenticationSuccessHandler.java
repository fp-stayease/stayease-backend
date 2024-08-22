package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Data
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  public final JwtService jwtService;
  public final UserService userService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    Optional<User> existingUser = userService.findByEmail(email);
    if (existingUser.isPresent()) {
      String token = jwtService.generateToken(existingUser.get());
      sendTokenResponse(response, token);
    } else {
      // Redirect to FE page for user type selection
      response.sendRedirect("/select-user-type?email=" + email);
    }

  }

  private void sendTokenResponse(HttpServletResponse response, String token) throws IOException {
    response.setContentType("application/json");
    response.getWriter().write("{\"token\":\"" + token + "\"}");
  }

}
