package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.repository.SocialLoginRepository;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.UserService;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final SocialLoginRepository socialLoginRepository;
  private final TenantInfoRepository tenantInfoRepository;

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  public void saveUser(User user) {
    userRepository.save(user);
  }

  @Override
  public void saveSocialLogin(SocialLogin socialLogin) {
    socialLoginRepository.save(socialLogin);
  }

  @Override
  public void saveTenantInfo(TenantInfo tenantInfo) {
    tenantInfoRepository.save(tenantInfo);
  }
}
