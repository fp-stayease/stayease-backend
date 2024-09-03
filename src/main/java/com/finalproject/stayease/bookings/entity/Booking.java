package com.finalproject.stayease.bookings.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finalproject.stayease.bookings.dto.BookingResDto;
import com.finalproject.stayease.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "status")
    private String status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BookingItem> bookingItems;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private BookingRequest bookingRequest;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Payment payment;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant updatedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    @PreRemove
    public void preRemove() {
        this.deletedAt = Instant.now();
    }

    public BookingResDto toResDto() {
        BookingResDto resDto = new BookingResDto();
        resDto.setId(this.id);
        resDto.setUserId(this.userId);
        resDto.setTenantId(this.tenantId);
        resDto.setCreatedAt(this.createdAt);
        resDto.setStatus(this.status);
        resDto.setTotalPrice(this.totalPrice);
        resDto.setBookingItems(this.bookingItems.stream().map(BookingItem::toResDto).toList());
        resDto.setBookingRequest(this.bookingRequest.toResDto());
        resDto.setPayment(this.payment.toResDto());

        return resDto;
    }
}
