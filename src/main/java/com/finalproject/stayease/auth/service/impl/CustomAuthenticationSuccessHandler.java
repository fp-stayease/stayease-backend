package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.service.UserService;
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
      Authentication authentication) throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    Optional<User> existingUser = userService.findByEmail(email);
    if (existingUser.isPresent()) {
      String token = jwtService.generateToken(existingUser.get());
      sendTokenResponse(response, token);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("User not found");
    }

  }

  private void sendTokenResponse(HttpServletResponse response, String token) throws IOException {
    response.setContentType("application/json");
    response.getWriter().write("{\"token\":\"" + token + "\"}");
  }

//  private UserInfo fetchGoogleUserInfo(String accessToken) {
//    RestTemplate restTemplate = new RestTemplate();
//    HttpHeaders headers = new HttpHeaders();
//    headers.setBearerAuth(accessToken);
//
//    HttpEntity<String> entity = new HttpEntity<>(headers);
//
//    ResponseEntity<UserInfo> response = restTemplate.exchange(
//        "https://www.googleapis.com/oauth2/v3/userinfo",
//        HttpMethod.GET, entity, UserInfo.class);
//
//    return response.getBody();
//  }

//  private User registerAndLinkNewUser(UserInfo userInfo) {
//    User newUser = new User();
//    newUser.setEmail(userInfo.getEmailAddress());
//    newUser.setFirstName(userInfo.getGivenName());
//    newUser.setLastName(userInfo.getFamilyName());
//    newUser.setAvatar(userInfo.getPicture());
//    newUser.setUserType("USER"); // or retrieve this based on selection
//    newUser.setVerified(true); // Assuming OAuth2 verifies email
//    userService.save(newUser);
//
//    // Save Social Login info
//    SocialLogin socialLogin = new SocialLogin();
//    socialLogin.setUser(newUser);
//    socialLogin.setProvider(userInfo.getProvider());
//    socialLogin.setProviderUserId(userInfo.getProviderUserId());
//    socialLoginService.save(socialLogin);
//
//    return newUser;
//  }

}
