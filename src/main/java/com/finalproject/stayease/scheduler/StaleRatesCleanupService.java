package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.property.service.PeakSeasonRateService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
@Data
public class StaleRatesCleanupService {

  private final PeakSeasonRateService peakSeasonRateService;

  @Scheduled(cron = "${cron.cleanup.stale-data:0 0 */12 * * ?}")
  public void cleanupStaleRates() {
    log.info("Cleaning up stale rates...");
    Instant timestamp = Instant.now().minus(12, ChronoUnit.HOURS);
    int deletedRates = peakSeasonRateService.hardDeleteStaleRates(timestamp);
    log.info("Deleted {} stale rates", deletedRates);
  }

}
