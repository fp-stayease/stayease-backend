package com.finalproject.stayease.users.service;

import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;

public interface SocialLoginService {

  void save(SocialLogin socialLogin);
  Users registerOAuth2User(SocialLoginRequest request);
  Optional<SocialLogin> findByUser(Users user);

  // Region - quarantine (delete if by the end not needed)
  Optional<SocialLogin> findByKey(String provider, String providerUserId);
}
