package com.finalproject.stayease.users.entity;

import com.finalproject.stayease.users.entity.User.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "pending_registration")
public class PendingRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_registration_id_gen")
  @SequenceGenerator(name = "pending_registration_id_gen", sequenceName = "pending_registration_id_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Long id;

  @Size(max = 255)
  @NotNull
  @Column(name = "email", nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(name = "user_type", nullable = false, length = 10)
  private UserType userType;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "last_verification_attempt")
  private OffsetDateTime lastVerificationAttempt;

  @Column(name = "verified_at")
  private OffsetDateTime verifiedAt;

}