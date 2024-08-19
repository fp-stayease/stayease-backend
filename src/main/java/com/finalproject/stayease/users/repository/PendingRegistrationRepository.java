package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.PendingRegistration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
  Optional<PendingRegistration> findByEmail(String email);
  void deleteByCreatedAtBefore(Instant expirationThreshold);
}
