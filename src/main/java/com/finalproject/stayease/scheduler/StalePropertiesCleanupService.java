package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.property.service.PropertyService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service responsible for cleaning up stale properties.
 *
 * This service is scheduled to run periodically based on the cron expression
 * defined in the application properties (`cron.cleanup.stale-data`). It identifies
 * properties that have been marked as deleted for more than 48 hours and performs
 * a hard delete on them.
 *
 * The cleanup process involves:
 * - Identifying properties with a `deletedAt` timestamp older than 48 hours.
 * - Hard deleting these properties from the database.
 * - Hard deleting all related rooms and room availability records associated with these properties
 *   through the `ON DELETE CASCADE` operation.
 *
 * Note: Before a property is marked for deletion, it undergoes a check to ensure there are no
 * related bookings. Therefore, this hard delete operation is safe to perform.
 *
 * The number of deleted properties is logged after each cleanup operation.
 */

@Service
@Slf4j
@Data
public class StalePropertiesCleanupService {

  private final PropertyService propertyService;

  @Scheduled(cron = "${cron.cleanup.stale-data:0 0 * * * ?}")
  public void cleanupStaleProperties() {
    log.info("Cleaning up stale properties...");
    Instant threshold = Instant.now().minus(48, ChronoUnit.HOURS);
    int deletedProperties = propertyService.hardDeleteStaleProperties(threshold);
    log.info("Deleted {} stale properties", deletedProperties);
  }

}