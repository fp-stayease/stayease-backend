package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.exceptions.users.TenantInfoNotFoundException;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.service.TenantInfoService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class TenantInfoServiceImpl implements TenantInfoService {
  private final TenantInfoRepository tenantInfoRepository;

  @Override
  public TenantInfo save(TenantInfo tenantInfo) {
    return tenantInfoRepository.save(tenantInfo);
  }

  @Override
  public Optional<TenantInfo> findByTenant(Users tenant) {
    return tenantInfoRepository.findByUser(tenant);
  }

  @Override
  public TenantInfo findTenantByUserId(Long userId) {
      return tenantInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new TenantInfoNotFoundException("Tenant not found"));
  }

  @Override
  public TenantInfoResDto getTenantDetail(Long tenantId) {
    var tenant = tenantInfoRepository.findById(tenantId)
            .orElseThrow(() -> new TenantInfoNotFoundException("Tenant not found"));
    return toResDto(tenant);
  }

  public TenantInfoResDto toResDto(TenantInfo tenantInfo) {
    TenantInfoResDto resDto = new TenantInfoResDto();
    resDto.setId(tenantInfo.getId());
    resDto.setUser(tenantInfo.getUser().toResDto());
    resDto.setBusinessName(tenantInfo.getBusinessName());
    resDto.setRegisterDate(tenantInfo.getRegistrationDate());
    return resDto;
  }
}
