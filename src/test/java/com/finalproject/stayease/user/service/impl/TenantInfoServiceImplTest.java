package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.users.TenantInfoNotFoundException;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.service.impl.TenantInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TenantInfoServiceImplTest {

  @Mock
  private TenantInfoRepository tenantInfoRepository;

  @InjectMocks
  private TenantInfoServiceImpl tenantInfoService;

  @Test
  void save_Success() {
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.save(any(TenantInfo.class))).thenReturn(tenantInfo);

    TenantInfo result = tenantInfoService.save(tenantInfo);

    assertNotNull(result);
    verify(tenantInfoRepository).save(tenantInfo);
  }

  @Test
  void findByTenant_Success() {
    Users tenant = new Users();
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.findByUser(tenant)).thenReturn(Optional.of(tenantInfo));

    Optional<TenantInfo> result = tenantInfoService.findByTenant(tenant);

    assertTrue(result.isPresent());
    assertEquals(tenantInfo, result.get());
  }

  @Test
  void findTenantByUserId_Success() {
    Long userId = 1L;
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.findByUserId(userId)).thenReturn(Optional.of(tenantInfo));

    TenantInfo result = tenantInfoService.findTenantByUserId(userId);

    assertNotNull(result);
    assertEquals(tenantInfo, result);
  }

  @Test
  void findTenantByUserId_NotFound_ThrowsException() {
    Long userId = 1L;
    when(tenantInfoRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThrows(TenantInfoNotFoundException.class, () -> tenantInfoService.findTenantByUserId(userId));
  }

  @Test
  void getTenantDetail_Success() {
    Long tenantId = 1L;
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setId(tenantId);
    tenantInfo.setUser(new Users());
    tenantInfo.setBusinessName("Test Business");
    tenantInfo.setRegistrationDate(Instant.now());

    when(tenantInfoRepository.findById(tenantId)).thenReturn(Optional.of(tenantInfo));

    TenantInfoResDto result = tenantInfoService.getTenantDetail(tenantId);

    assertNotNull(result);
    assertEquals(tenantId, result.getId());
    assertEquals(tenantInfo.getBusinessName(), result.getBusinessName());
    assertEquals(tenantInfo.getRegistrationDate(), result.getRegisterDate());
  }

  @Test
  void getTenantDetail_NotFound_ThrowsException() {
    Long tenantId = 1L;
    when(tenantInfoRepository.findById(tenantId)).thenReturn(Optional.empty());

    assertThrows(TenantInfoNotFoundException.class, () -> tenantInfoService.getTenantDetail(tenantId));
  }
}
