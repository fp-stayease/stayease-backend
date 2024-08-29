package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.PendingRegistration;
import java.time.Instant;
import java.util.Optional;

public interface PendingRegistrationService {

  void save(PendingRegistration pendingRegistration);
  Optional<PendingRegistration> findByEmail(String email);

  void deleteById(Long id);
  void deleteExpired(Instant expiredThreshold);
}
