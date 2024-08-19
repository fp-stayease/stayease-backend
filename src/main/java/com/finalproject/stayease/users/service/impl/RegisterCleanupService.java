package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Data
@Service
public class RegisterCleanupService {

  private final PendingRegistrationRepository registrationRepository;

  @Scheduled(cron = "${pending.cleanup.cron:0 0 */12 * * ?}")
  public void cleanupExpiredPendingRegistrations() {
    Instant now = Instant.now();
    Instant expirationThreshold = now.minus(1, ChronoUnit.DAYS);
    registrationRepository.deleteByCreatedAtBefore(expirationThreshold);
  }
}
