package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  Optional<User> findByEmail(String email);


  // Region - Quarantine
  Optional<User> findById(Long id);
  void saveUser(User user);
  void saveSocialLogin(SocialLogin socialLogin);
  void saveTenantInfo(TenantInfo tenantInfo);
}
