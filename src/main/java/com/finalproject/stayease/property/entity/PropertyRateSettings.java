package com.finalproject.stayease.property.entity;

import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
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
public class PropertyRateSettings {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "property_rate_settings_id_gen")
  @SequenceGenerator(name = "property_rate_settings_id_gen", sequenceName = "property_rate_settings_id_seq", allocationSize = 1)
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
  private BigDecimal holidayAdjustmentRate;

  @Enumerated(EnumType.STRING)
  @Column(name = "holiday_adjustment_type")
  private PeakSeasonRate.AdjustmentType holidayAdjustmentType;

  @Column(name = "long_weekend_rate_adjustment", precision = 10, scale = 2)
  private BigDecimal longWeekendAdjustmentRate;

  @Enumerated(EnumType.STRING)
  @Column(name = "long_weekend_adjustment_type")
  private AdjustmentType longWeekendAdjustmentType;

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

}