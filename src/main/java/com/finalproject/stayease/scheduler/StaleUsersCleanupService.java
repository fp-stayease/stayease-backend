package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.users.service.UsersService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Data
public class StaleUsersCleanupService {

  private final UsersService usersService;

  @Scheduled(cron = "${cron.cleanup.stale-data:0 0 * * * ?}")
  public void cleanupStaleUsers() {
    log.info("Cleaning up stale users...");
    Instant timestamp = Instant.now().minus(48, ChronoUnit.HOURS);
    int deletedUser = usersService.hardDeleteStaleUsers(timestamp);
    log.info("Deleted {} stale users", deletedUser);
  }
}
