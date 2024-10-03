package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.entity.UserAuth;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@EqualsAndHashCode(callSuper = true)
@Service
@Data
@Transactional
public class  CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final SocialLoginService socialLoginService;
  private final UsersService usersService;
  private final HttpSession session;


  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();
    String providerUserId = extractProviderId(oAuth2User, provider);
    String email = oAuth2User.getAttribute("email");

    Optional<Users> userOptional = usersService.findByEmail(email);


    if (userOptional.isPresent()) {
      return new DefaultOAuth2User(extractAuthorities(userOptional.get()), oAuth2User.getAttributes(), "email");
    } else {
      // Store OAuth2 user info in session for later use
      session.setAttribute("oAuth2UserInfo", oAuth2User.getAttributes());
      session.setAttribute("provider", provider);
      session.setAttribute("providerUserId", providerUserId);
      // Create a temporary user with a special role
      return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_SELECT_USER_TYPE")),
          oAuth2User.getAttributes(), "email");
    }
  }

  private String extractProviderId(OAuth2User oauth2User, String provider) {
    Map<String, Object> attributes = oauth2User.getAttributes();
    return switch (provider.toLowerCase()) {
      case "google" -> (String) attributes.get("sub");
      case "github" -> attributes.get("id").toString();
      default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
    };
  }

  private Collection<? extends GrantedAuthority> extractAuthorities(Users user) {
    UserAuth userAuth = new UserAuth(user);
    return userAuth.getAuthorities();
  }

}
