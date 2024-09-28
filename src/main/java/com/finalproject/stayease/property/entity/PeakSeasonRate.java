package com.finalproject.stayease.property.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "peak_season_rate")
public class PeakSeasonRate {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "peak_season_rate_id_gen")
  @SequenceGenerator(name = "peak_season_rate_id_gen", sequenceName = "peak_season_rate_id_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id")
  private Property property;

  @NotNull
  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @NotNull
  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @NotNull
  @Column(name = "adjustment_rate", nullable = false, precision = 10, scale = 2)
  private BigDecimal adjustmentRate;

  @Enumerated(EnumType.STRING)
  @Column(name = "adjustment_type", length = 10)
  private AdjustmentType adjustmentType;

  @Column(name = "reason", length = Integer.MAX_VALUE)
  private String reason;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "valid_from")
  private Instant validFrom;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;


  @PrePersist
  protected void onCreate() {
    validFrom = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  @PreRemove
  protected void onDelete() {
    deletedAt = Instant.now();
  }

  public enum AdjustmentType {
    PERCENTAGE,
    FIXED
  }

}