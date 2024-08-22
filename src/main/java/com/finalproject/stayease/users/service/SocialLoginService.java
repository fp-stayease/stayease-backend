package com.finalproject.stayease.users.service;

import com.finalproject.stayease.auth.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.dto.SocialLoginResponse;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.User.UserType;
import java.util.Optional;

public interface SocialLoginService {

  SocialLoginResponse socialLogin(SocialLoginRequest request, UserType userType);
  Optional<SocialLogin> findByKey(String provider, String providerUserId);
}
