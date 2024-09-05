package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.TenantInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantInfoRepository extends JpaRepository<TenantInfo, Long> {
    Optional<TenantInfo> findByUserId(Long userId);
}
