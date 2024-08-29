package com.finalproject.stayease.bookings.entity;

import com.finalproject.stayease.bookings.dto.BookingItemResDto;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "checkin_date")
    private Date checkInDate;

    @Column(name = "checkout_date")
    private Date checkOutDate;

    @Column(name = "price")
    private Double price;

    @Column(name = "total_adults")
    private int totalAdults;

    @Column(name = "total_children")
    private int totalChildren;

    @Column(name = "total_infants")
    private int totalInfants;

    @Column(name = "is_extending")
    private boolean isExtending;

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

    public BookingItemResDto toResDto() {
        BookingItemResDto resDto = new BookingItemResDto();
        resDto.setBasePrice(this.price);
        resDto.setRoomId(this.roomId);
        resDto.setCheckInDate(this.checkInDate);
        resDto.setCheckOutDate(this.checkOutDate);
        resDto.setTotalAdults(this.totalAdults);
        resDto.setTotalChildren(this.totalChildren);
        resDto.setTotalInfants(this.totalInfants);
        resDto.setExtending(this.isExtending);
        return resDto;
    }
}
