package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.dto.SocialLoginResponse;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.repository.SocialLoginRepository;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.SocialLoginService;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class SocialLoginServiceImpl implements SocialLoginService {

  private final UserRepository userRepository;
  private final SocialLoginRepository socialLoginRepository;
  private final TenantInfoRepository tenantInfoRepository;

  @Override
  public SocialLoginResponse socialLogin(SocialLoginRequest request, UserType userType) {
    String provider = request.getProvider();
    String providerUserId = request.getProviderUserId();
    String email = request.getEmail();

    Optional<SocialLogin> existingSocialLogin = socialLoginRepository.findByProviderAndProviderUserId(provider, providerUserId);

    if (existingSocialLogin.isPresent()) {
      User user = existingSocialLogin.get().getUser();
      return new SocialLoginResponse(user, null); // Add JWT token generation here
    }

    Optional<User> existingUser = userRepository.findByEmail(email);

    if (existingUser.isPresent()) {
      User user = existingUser.get();
      linkSocialLogin(user, provider, providerUserId);
      return new SocialLoginResponse(user, null); // Add JWT token generation here
    }

    User newUser = createNewUser(request, userType);
    linkSocialLogin(newUser, provider, providerUserId);

    if (userType == UserType.TENANT) {
      TenantInfo tenantInfo = createTenantInfo(newUser, request);
      return new SocialLoginResponse(newUser, tenantInfo, null); // Add JWT token generation here
    }

    return new SocialLoginResponse(newUser, null); // Add JWT token generation here
  }

  private User createNewUser(SocialLoginRequest request, UserType userType) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setUserType(userType);
    user.setIsVerified(true);
    return userRepository.save(user);
  }

  private void linkSocialLogin(User user, String provider, String providerUserId) {
    SocialLogin socialLogin = new SocialLogin();
    socialLogin.setUser(user);
    socialLogin.setProvider(provider);
    socialLogin.setProviderUserId(providerUserId);
    socialLoginRepository.save(socialLogin);
  }

  private TenantInfo createTenantInfo(User user, SocialLoginRequest request) {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setUser(user);
    tenantInfo.setBusinessName(request.getBusinessName());
    tenantInfo.setTaxId(request.getTaxId());
    return tenantInfoRepository.save(tenantInfo);
  }

}
