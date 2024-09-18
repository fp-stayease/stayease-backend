package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantInfoRepository extends JpaRepository<TenantInfo, Long> {
  Optional<TenantInfo> findByUser(Users user);
  Optional<TenantInfo> findByUserId(Long userId);
}
