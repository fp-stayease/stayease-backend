package com.finalproject.stayease.bookings.entity;

import com.finalproject.stayease.bookings.dto.BookingItemResDto;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.RoomDTO;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "extending_until")
    private LocalDate extendingUntil;

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
        resDto.setRoom(new RoomDTO(this.room));
        resDto.setExtendingUntil(this.extendingUntil);
        return resDto;
    }
}
