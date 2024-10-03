package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.repository.SocialLoginRepository;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
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
    if (request.getUserType() == UserType.TENANT) {
      createTenantInfo(user, request);
    }
    return user;
  }

  @Override
  public Optional<SocialLogin> findByUser(Users user) {
    return socialLoginRepository.findByUser(user);
  }

  // Helpers
  private Users createNewUser(SocialLoginRequest request) {
    Users user = new Users();
    user.setEmail(request.getEmail());
    user.setUserType(request.getUserType());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setAvatar(request.getAvatar());
    user.setIsVerified(true);
    usersService.save(user);
    log.info("user registered: " + request.getEmail());
    return user;
  }

  private void linkSocialLogin(Users user, String provider, String providerUserId) {
    SocialLogin socialLogin = new SocialLogin();
    socialLogin.setUser(user);
    socialLogin.setProvider(provider);
    socialLogin.setProviderUserId(providerUserId);
    socialLoginRepository.save(socialLogin);
    log.info("link social login: {}", socialLogin.getProvider());
  }

  private TenantInfo createTenantInfo(Users user, SocialLoginRequest request) {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setUser(user);
    tenantInfo.setBusinessName(Optional.ofNullable(request.getBusinessName()).orElse(user.getFirstName()));
    tenantInfo.setTaxId(request.getTaxId());
    tenantInfoService.save(tenantInfo);
    return tenantInfo;
  }

  // Region - Quarantine

  @Override
  public Optional<SocialLogin> findByKey(String provider, String providerUserId) {
    return socialLoginRepository.findByProviderAndProviderUserId(provider, providerUserId);
  }

}
