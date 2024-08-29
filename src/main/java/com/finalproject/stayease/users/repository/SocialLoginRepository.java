package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

  Optional<SocialLogin> findByProviderAndProviderUserId(String providerUserId, String providerUserName);
  Optional<SocialLogin> findByUser(Users user);
}
