package com.finalproject.stayease.property.entity;

import com.finalproject.stayease.bookings.entity.BookingItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "room")
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_id_gen")
  @SequenceGenerator(name = "room_id_gen", sequenceName = "room_id_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id")
  private Property property;

  @Size(max = 100)
  @NotNull
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @NotNull
  @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal basePrice;

  @NotNull
  @Column(name = "capacity", nullable = false)
  private Integer capacity;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at")
  private Instant createdAt;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @OneToMany(mappedBy = "room")
  private Set<RoomAvailability> roomAvailabilities = new LinkedHashSet<>();

  @Column(name = "imageUrl", length = Integer.MAX_VALUE)
  private String imageUrl;

  @OneToMany(mappedBy = "room")
  private Set<BookingItem> bookingItems = new LinkedHashSet<>();

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