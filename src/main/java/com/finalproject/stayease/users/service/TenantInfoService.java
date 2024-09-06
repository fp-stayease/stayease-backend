package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.entity.TenantInfo;

public interface TenantInfoService {

  void save(TenantInfo tenantInfo);
  TenantInfo findTenantByUserId(Long userId);
  TenantInfoResDto getTenantDetail(Long tenantId);
}
