package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.Users;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

  Optional<Users> findByEmail(String email);

  @Query("SELECT u.avatar FROM Users u")
  List<String> findAllAvatars();

  @Modifying
  @Query("""
      DELETE FROM Users u
      WHERE u.deletedAt IS NOT NULL
      AND u.deletedAt < :timestamp
      """)
  int hardDeleteStaleUsers(Instant timestamp);
}
