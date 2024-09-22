package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.users.service.PendingRegistrationService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Data
@Service
@Transactional
public class RegisterCleanupService {

  private final PendingRegistrationService pendingRegistrationService;

  @Scheduled(cron = "${cron.cleanup.pending:0 0 */12 * * ?}")
  public void cleanupExpiredPendingRegistrations() {
    Instant now = Instant.now();
    Instant expirationThreshold = now.minus(1, ChronoUnit.DAYS);
    pendingRegistrationService.deleteExpired(expirationThreshold);
  }
}
