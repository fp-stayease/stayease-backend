package com.finalproject.stayease.property.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
  @ColumnDefault("nextval('peak_season_rate_id_seq'::regclass)")
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
  @Column(name = "rate_adjustment", nullable = false, precision = 10, scale = 2)
  private BigDecimal rateAdjustment;

  @Enumerated(EnumType.STRING)
  @Column(name = "adjustment_type", length = 10)
  private AdjustmentType adjustmentType;

  @Column(name = "reason", length = Integer.MAX_VALUE)
  private String reason;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "valid_from")
  private Instant validFrom;

  @Column(name = "valid_to")
  private Instant validTo;

  @PrePersist
  protected void onCreate() {
    validFrom = Instant.now();
  }

  public enum AdjustmentType {
    PERCENTAGE,
    FIXED
  }

}