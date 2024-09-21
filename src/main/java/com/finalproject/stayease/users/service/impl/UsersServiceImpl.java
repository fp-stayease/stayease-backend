package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.repository.UsersRepository;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Data
@Service
@Transactional
@Slf4j
public class UsersServiceImpl implements UsersService {

  private final UsersRepository usersRepository;


  public UsersServiceImpl(UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
  }

  @Override
  public Optional<Users> findByEmail(String email) {
    return usersRepository.findByEmail(email);
  }

  @Override
  public Users getLoggedUser() throws RuntimeException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AccessDeniedException("You must be logged in to access this resource");
    }
    String email = authentication.getName();
    return findByEmail(email).orElseThrow(() -> new AccessDeniedException("You must be logged in to access this resource"));
  }

  @Override
  public void deleteUser(Users user) {
    user.setDeletedAt(Instant.now());
    usersRepository.save(user);
  }

  @Override
  public List<String> findAllAvatars() {
    return usersRepository.findAllAvatars();
  }

  @Override
  public Optional<Users> findById(Long id) {
    return usersRepository.findById(id);
  }

  @Override
  public int hardDeleteStaleUsers(Instant timestamp) {
    return usersRepository.hardDeleteStaleUsers(timestamp);
  }

  @Override
  public Users save(Users user) {
    return usersRepository.save(user);
  }

}
