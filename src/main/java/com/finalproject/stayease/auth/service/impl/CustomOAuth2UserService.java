package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.model.entity.UserAuth;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UserService;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@EqualsAndHashCode(callSuper = true)
@Service
@Data
public class  CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final SocialLoginService socialLoginService;
  private final UserService userService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();
    String providerUserId = extractProviderId(oAuth2User, provider);
    String email = oAuth2User.getAttribute("email");
    String firstName = oAuth2User.getAttribute("given_name");
    String lastName = oAuth2User.getAttribute("family_name");
    String pictureUrl = oAuth2User.getAttribute("picture");

    Optional<User> userOptional = userService.findByEmail(email);
    User user;

    if (userOptional.isPresent()) {
      user = userOptional.get();
    } else {
      // !! TODO : User is by default a 'USER' don't forget to implement change userType from front end
      SocialLoginRequest request = new SocialLoginRequest(provider, providerUserId, email, firstName, lastName, pictureUrl);
      user = socialLoginService.registerOAuth2User(request);
    }

    return new DefaultOAuth2User(extractAuthorities(user), oAuth2User.getAttributes(), "email");
  }

  private String extractProviderId(OAuth2User oauth2User, String provider) {
    if ("google".equals(provider)) {
      return oauth2User.getAttribute("sub");
    } else if ("github".equals(provider)) {
      return Objects.requireNonNull(oauth2User.getAttribute("id")).toString();
    }
    throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
  }

  private Collection<? extends GrantedAuthority> extractAuthorities(User user) {
    UserAuth userAuth = new UserAuth(user);
    return userAuth.getAuthorities();
  }

}
