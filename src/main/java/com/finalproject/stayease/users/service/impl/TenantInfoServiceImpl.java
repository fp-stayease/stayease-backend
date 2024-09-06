package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
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

  @Override
  public TenantInfo findTenantByUserId(Long userId) {
      return tenantInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new DataNotFoundException("Tenant not found"));
  }

  @Override
  public TenantInfoResDto getTenantDetail(Long tenantId) {
    var tenant = tenantInfoRepository.findById(tenantId)
            .orElseThrow(() -> new DataNotFoundException("Tenant not found"));
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
