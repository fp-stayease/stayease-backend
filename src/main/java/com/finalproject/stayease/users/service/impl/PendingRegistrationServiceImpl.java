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

  @Override
  public void save(PendingRegistration pendingRegistration) {
    pendingRegistrationRepository.save(pendingRegistration);
  }

  @Override
  public Optional<PendingRegistration> findByEmail(String email) {
    return pendingRegistrationRepository.findByEmail(email);
  }

  @Override
  public void deleteById(Long id) {
    pendingRegistrationRepository.deleteById(id);
  }

  @Override
  public void deleteExpired(Instant expiredThreshold) {
    pendingRegistrationRepository.deleteExpired(expiredThreshold);
  }
}
