package com.finalproject.stayease.property.entity;

import com.finalproject.stayease.users.entity.Users;
import jakarta.persistence.CascadeType;
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
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Entity
@Table(name = "property")
public class Property {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "property_id_gen")
  @SequenceGenerator(name = "property_id_gen", sequenceName = "property_id_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  @NotNull(message = "Only tenants can create property.")
  private Users tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  @NotNull(message = "Must include property")
  private PropertyCategory category;

  @Size(max = 255)
  @NotNull(message = "Must include name")
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  @NotNull(message = "Must include description")
  private String description;

  @Column(name = "images", length = Integer.MAX_VALUE)
  @NotNull(message = "Must include picture") //TODO: update with picture inclusion
  private String images;

  @Size(max = 255)
  @NotNull(message = "Must include address")
  @Column(name = "address", nullable = false)
  private String address;

  @Size(max = 100)
  @NotNull(message = "Must include city")
  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Size(max = 100)
  @NotNull(message = "Must include country")
  @Column(name = "country", nullable = false, length = 100)
  private String country;

  @Column(name = "location", columnDefinition = "geography(Point, 4326)")
  @NotNull(message = "Must include location")
  private Point location;

  @Column(name = "longitude")
  @NotNull(message = "Must include longitude")
  private Double longitude;

  @Column(name = "latitude")
  @NotNull(message = "Must include latitude")
  private Double latitude;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at")
  private Instant createdAt;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @OneToMany(mappedBy = "property", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<PeakSeasonRate> peakSeasonRates = new LinkedHashSet<>();

  @OneToMany(mappedBy = "property", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<Room> rooms = new LinkedHashSet<>();

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

/*
 TODO [Reverse Engineering] create field to map the 'location' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "location", columnDefinition = "geography")
    private Object location;
*/
}