package com.finalproject.stayease.user.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.finalproject.stayease.users.service.PendingRegistrationService;
import com.finalproject.stayease.scheduler.RegisterCleanupService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class RegisterCleanupServiceTest {
  @MockBean
  private PendingRegistrationService pendingRegistrationService;

  @InjectMocks
  private RegisterCleanupService registerCleanupService = new RegisterCleanupService(pendingRegistrationService);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    registerCleanupService = new RegisterCleanupService(pendingRegistrationService);
  }

  @Test
  void cleanupExpiredPendingRegistrations_shouldDeleteExpiredRegistrations() {
    // Arrange
    Instant now = Instant.now();
    Instant expirationThreshold = now.minus(1, ChronoUnit.DAYS);

    // Act
    registerCleanupService.cleanupExpiredPendingRegistrations();

    // Assert
    verify(pendingRegistrationService, times(1)).deleteExpired(expirationThreshold);
  }

}
