package com.finalproject.stayease.user.service.impl;

import com.finalproject.stayease.exceptions.users.TenantInfoNotFoundException;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.TenantInfoDTO;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.service.impl.TenantInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantInfoServiceImplTest {

  @Mock
  private TenantInfoRepository tenantInfoRepository;

  @InjectMocks
  private TenantInfoServiceImpl tenantInfoService;

  @Test
  void testSave() {
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.save(tenantInfo)).thenReturn(tenantInfo);

    TenantInfo savedTenantInfo = tenantInfoService.save(tenantInfo);

    assertNotNull(savedTenantInfo);
    verify(tenantInfoRepository).save(tenantInfo);
  }

  @Test
  void testFindByTenant() {
    Users user = new Users();
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.findByUser(user)).thenReturn(Optional.of(tenantInfo));

    Optional<TenantInfo> result = tenantInfoService.findByTenant(user);

    assertTrue(result.isPresent());
    assertEquals(tenantInfo, result.get());
    verify(tenantInfoRepository).findByUser(user);
  }

  @Test
  void testFindTenantByUserId_Found() {
    Long userId = 1L;
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.findByUserId(userId)).thenReturn(Optional.of(tenantInfo));

    TenantInfo result = tenantInfoService.findTenantByUserId(userId);

    assertNotNull(result);
    assertEquals(tenantInfo, result);
    verify(tenantInfoRepository).findByUserId(userId);
  }

  @Test
  void testFindTenantByUserId_NotFound() {
    Long userId = 1L;
    when(tenantInfoRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThrows(TenantInfoNotFoundException.class, () -> tenantInfoService.findTenantByUserId(userId));
    verify(tenantInfoRepository).findByUserId(userId);
  }

  @Test
  void testGetTenantDetail_Found() {
    Long tenantId = 1L;
    TenantInfo tenantInfo = new TenantInfo();
    when(tenantInfoRepository.findById(tenantId)).thenReturn(Optional.of(tenantInfo));

    TenantInfoDTO result = tenantInfoService.getTenantDetail(tenantId);

    assertNotNull(result);
    verify(tenantInfoRepository).findById(tenantId);
  }

  @Test
  void testGetTenantDetail_NotFound() {
    Long tenantId = 1L;
    when(tenantInfoRepository.findById(tenantId)).thenReturn(Optional.empty());

    assertThrows(TenantInfoNotFoundException.class, () -> tenantInfoService.getTenantDetail(tenantId));
    verify(tenantInfoRepository).findById(tenantId);
  }
}