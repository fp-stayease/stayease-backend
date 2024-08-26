package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.model.dto.SocialLoginResponse;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.repository.SocialLoginRepository;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class SocialLoginServiceImpl implements SocialLoginService {

  private final SocialLoginRepository socialLoginRepository;
  private final UsersService usersService;
  private final TenantInfoService tenantInfoService;

  @Override
  public void save(SocialLogin socialLogin) {
    socialLoginRepository.save(socialLogin);
  }

  @Override
  public Users registerOAuth2User(SocialLoginRequest request) {
    Users user = createNewUser(request);
    linkSocialLogin(user, request.getProvider(), request.getProviderUserId());
    return user;
  }

  @Override
  public void changeUserType(UserType userType) {
    Users existingUser = usersService.getLoggedUser();
    Optional<SocialLogin> socialLoginOptional = socialLoginRepository.findByUser(existingUser);
    if (socialLoginOptional.isEmpty()) {
      // TODO : make NoLinkedSocialLoginException
      throw new RuntimeException("Account not linked to any social login");
    }

    existingUser.setUserType(userType);
    usersService.save(existingUser);
  }

  // Helpers
  private Users createNewUser(SocialLoginRequest request) {
    Users user = new Users();
    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setIsVerified(true);
    usersService.save(user);
    return user;
  }

  private void linkSocialLogin(Users user, String provider, String providerUserId) {
    SocialLogin socialLogin = new SocialLogin();
    socialLogin.setUser(user);
    socialLogin.setProvider(provider);
    socialLogin.setProviderUserId(providerUserId);
    socialLoginRepository.save(socialLogin);
  }

  private TenantInfo createTenantInfo(Users user, SocialLoginRequest request) {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setUser(user);
    tenantInfo.setBusinessName(request.getBusinessName());
    tenantInfo.setTaxId(request.getTaxId());
    tenantInfoService.save(tenantInfo);
    return tenantInfo;
  }

  // Region - Quarantine

  @Override
  public SocialLoginResponse socialLogin(SocialLoginRequest request, UserType userType) {
    String provider = request.getProvider();
    String providerUserId = request.getProviderUserId();
    String email = request.getEmail();

    Optional<SocialLogin> existingSocialLogin = socialLoginRepository.findByProviderAndProviderUserId(provider, providerUserId);

    if (existingSocialLogin.isPresent()) {
      Users user = existingSocialLogin.get().getUser();
      return new SocialLoginResponse(user, null); // Add JWT token generation here
    }

    Optional<Users> existingUser = usersService.findByEmail(email);

    if (existingUser.isPresent()) {
      Users user = existingUser.get();
      linkSocialLogin(user, provider, providerUserId);
      return new SocialLoginResponse(user, null); // Add JWT token generation here
    }

    Users newUser = createNewUser(request);
    linkSocialLogin(newUser, provider, providerUserId);

    if (userType == UserType.TENANT) {
      TenantInfo tenantInfo = createTenantInfo(newUser, request);
      return new SocialLoginResponse(newUser, tenantInfo, null); // Add JWT token generation here
    }

    return new SocialLoginResponse(newUser, null); // Add JWT token generation here
  }

  @Override
  public Optional<SocialLogin> findByKey(String provider, String providerUserId) {
    return socialLoginRepository.findByProviderAndProviderUserId(provider, providerUserId);
  }

}
