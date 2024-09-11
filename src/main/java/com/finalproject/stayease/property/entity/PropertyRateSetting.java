package com.finalproject.stayease.property.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "property_rate_settings")
public class PropertyRateSetting {

  @Id
  @ColumnDefault("nextval('property_rate_settings_id_seq'::regclass)")
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  @NotNull
  @Column(name = "use_auto_rates", nullable = false)
  private Boolean useAutoRates = false;

  @Column(name = "holiday_rate_adjustment", precision = 10, scale = 2)
  private BigDecimal holidayRateAdjustment;

  @Size(max = 10)
  @Column(name = "holiday_adjustment_type", length = 10)
  private String holidayAdjustmentType;

  @Column(name = "long_weekend_rate_adjustment", precision = 10, scale = 2)
  private BigDecimal longWeekendRateAdjustment;

  @Size(max = 10)
  @Column(name = "long_weekend_adjustment_type", length = 10)
  private String longWeekendAdjustmentType;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "valid_from")
  private Instant validFrom;

  @Column(name = "valid_to")
  private Instant validTo;

  @PrePersist
  protected void onCreate() {
    validFrom = Instant.now();
  }

}