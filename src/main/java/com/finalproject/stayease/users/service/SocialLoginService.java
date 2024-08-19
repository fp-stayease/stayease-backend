package com.finalproject.stayease.users.service;

import com.finalproject.stayease.auth.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.dto.SocialLoginResponse;
import com.finalproject.stayease.users.entity.User.UserType;

public interface SocialLoginService {

  SocialLoginResponse socialLogin(SocialLoginRequest request, UserType userType);
}
