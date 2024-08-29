package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.service.TenantInfoService;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class TenantInfoServiceImpl implements TenantInfoService {
  private final TenantInfoRepository tenantInfoRepository;

  @Override
  public void save(TenantInfo tenantInfo) {
    tenantInfoRepository.save(tenantInfo);
  }
}
