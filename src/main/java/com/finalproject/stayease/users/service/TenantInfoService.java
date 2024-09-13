package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;

public interface TenantInfoService {

  TenantInfo save(TenantInfo tenantInfo);
  Optional<TenantInfo> findByTenant(Users tenant);
  TenantInfo findTenantByUserId(Long userId);
  TenantInfoResDto getTenantDetail(Long tenantId);
}
