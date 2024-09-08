package com.finalproject.stayease.users.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
  @SequenceGenerator(name = "users_id_gen", sequenceName = "users_id_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Long id;

  @Size(max = 255)
  @NotNull
  @Column(name = "email", nullable = false)
  private String email;

  @Size(max = 255)
  @Column(name = "password_hash")
  private String passwordHash;

  @Size(max = 100)
  @NotNull
  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Size(max = 100)
  @NotNull
  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Size(max = 20)
  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Size(max = 255)
  @Column(name = "avatar")
  private String avatar;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(name = "user_type", nullable = false)
  private UserType userType = UserType.USER;

  @ColumnDefault("false")
  @Column(name = "is_verified")
  private Boolean isVerified;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at")
  private Instant createdAt;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SocialLogin> socialLogins = new LinkedHashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenantInfo tenantInfo;

  public enum UserType {
    USER,
    TENANT
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}