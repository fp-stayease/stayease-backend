package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
  @Query("""
        SELECT ra
        FROM RoomAvailability ra
        WHERE :date BETWEEN ra.startDate AND ra.endDate
        AND ra.deletedAt IS NULL
    """)
  RoomAvailability findRoomAvailabilityByDate(@Param("date") LocalDate date);

  @Query("""
        SELECT ra
        FROM RoomAvailability ra
        WHERE ra.room.id = :roomId
        AND ra.startDate = :checkInDate
        AND ra.endDate = :checkOutDate
        AND ra.deletedAt IS NULL
    """)
  Optional<RoomAvailability> findByRoomIdAndDates(
          @Param("roomId") Long roomId,
          @Param("checkInDate") LocalDate checkInDate,
          @Param("checkOutDate") LocalDate checkOutDate
  );
}
