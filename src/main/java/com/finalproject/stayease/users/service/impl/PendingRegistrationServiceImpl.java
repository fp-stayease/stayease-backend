package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import com.finalproject.stayease.users.service.PendingRegistrationService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class PendingRegistrationServiceImpl implements PendingRegistrationService {

  private final PendingRegistrationRepository pendingRegistrationRepository;

  /**
   * Saves a pending registration.
   *
   * @param pendingRegistration The pending registration to save
   */
  @Override
  public void save(PendingRegistration pendingRegistration) {
    pendingRegistrationRepository.save(pendingRegistration);
  }

  /**
   * Finds a pending registration by email.
   *
   * @param email The email to search for
   * @return An Optional containing the pending registration if found, empty otherwise
   */
  @Override
  public Optional<PendingRegistration> findByEmail(String email) {
    return pendingRegistrationRepository.findByEmail(email);
  }

  /**
   * Deletes a pending registration by its ID.
   *
   * @param id The ID of the pending registration to delete
   */
  @Override
  public void deleteById(Long id) {
    pendingRegistrationRepository.deleteById(id);
  }

  /**
   * Deletes all expired pending registrations.
   *
   * @param expiredThreshold The threshold date for expiration
   */
  @Override
  public void deleteExpired(Instant expiredThreshold) {
    pendingRegistrationRepository.deleteExpired(expiredThreshold);
  }
}
