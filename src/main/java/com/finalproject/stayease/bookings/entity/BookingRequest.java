package com.finalproject.stayease.bookings.entity;

import com.finalproject.stayease.bookings.dto.BookingRequestResDto;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Date;

@Entity
@Data
@Table(name = "booking_requests")
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "checkin_time")
    private Date checkInTime;

    @Column(name = "checkout_time")
    private Date checkOutTime;

    @Column(name = "non_smoking")
    private boolean nonSmoking;

    @Column(name = "other")
    private String other;

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

    public BookingRequestResDto toResDto() {
        BookingRequestResDto resDto = new BookingRequestResDto();
        resDto.setCheckInTime(this.checkInTime);
        resDto.setCheckOutTime(this.checkOutTime);
        resDto.setNonSmoking(this.nonSmoking);
        resDto.setOther(this.other);

        return resDto;
    }
}
