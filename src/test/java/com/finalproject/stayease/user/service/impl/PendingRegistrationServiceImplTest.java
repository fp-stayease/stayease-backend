package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import com.finalproject.stayease.users.service.impl.PendingRegistrationServiceImpl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendingRegistrationServiceImplTest {

  @Mock
  private PendingRegistrationRepository pendingRegistrationRepository;

  @InjectMocks
  private PendingRegistrationServiceImpl pendingRegistrationService;

  @Test
  void save_Success() {
    PendingRegistration pendingRegistration = new PendingRegistration();
    pendingRegistrationService.save(pendingRegistration);
    verify(pendingRegistrationRepository).save(pendingRegistration);
  }

  @Test
  void findByEmail_Found() {
    String email = "test@example.com";
    PendingRegistration pendingRegistration = new PendingRegistration();
    when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.of(pendingRegistration));

    Optional<PendingRegistration> result = pendingRegistrationService.findByEmail(email);

    assertTrue(result.isPresent());
    assertEquals(pendingRegistration, result.get());
  }

  @Test
  void findByEmail_NotFound() {
    String email = "nonexistent@example.com";
    when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.empty());

    Optional<PendingRegistration> result = pendingRegistrationService.findByEmail(email);

    assertFalse(result.isPresent());
  }

  @Test
  void deleteById_Success() {
    Long id = 1L;
    pendingRegistrationService.deleteById(id);
    verify(pendingRegistrationRepository).deleteById(id);
  }

  @Test
  void deleteExpired_Success() {
    Instant expiredThreshold = Instant.now().minusSeconds(3600);
    pendingRegistrationService.deleteExpired(expiredThreshold);
    verify(pendingRegistrationRepository).deleteExpired(expiredThreshold);
  }
}
