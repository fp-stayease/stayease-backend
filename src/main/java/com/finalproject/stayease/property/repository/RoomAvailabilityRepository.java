package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
  @Query("""
    SELECT ra
    FROM RoomAvailability ra
    WHERE :date BETWEEN ra.startDate AND ra.endDate
   """)
  RoomAvailability findRoomAvailabilityByDate(@Param("date") LocalDate date);
}
