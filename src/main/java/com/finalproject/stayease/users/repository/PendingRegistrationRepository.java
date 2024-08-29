package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.PendingRegistration;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

  Optional<PendingRegistration> findByEmail(String email);

  @Modifying
  @Transactional
  @Query("DELETE FROM PendingRegistration pr WHERE pr.id = :id")
  void deleteById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("""
      DELETE FROM PendingRegistration pr WHERE pr.createdAt <= :date
      """)
  void deleteExpired(@Param("date") Instant expirationThreshold);
}
