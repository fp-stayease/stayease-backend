package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

  Optional<Room> findByNameIgnoreCaseAndDeletedAtIsNull(String roomName);

  Optional<Room> findByIdAndDeletedAtIsNull(Long id);
  List<Room> findAllByPropertyAndDeletedAtIsNull(Property propertyId);

  @Query("""
    SELECT r FROM Room r
    JOIN r.property p
    WHERE r.deletedAt IS NULL
    AND p.tenant.id = :tenantId
    ORDER BY p.name
    """)
  List<Room> findRoomByTenantIdAndDeletedAtIsNull(@Param("tenantId") Long tenantId);

  @Query("""
    SELECT r FROM Room r
    JOIN r.property p
    JOIN r.roomAvailabilities ra
    WHERE r.deletedAt IS NULL
    AND p.deletedAt IS NULL
    AND ra.deletedAt IS NULL
    AND ra.isAvailable = FALSE
    AND p.tenant.id = :tenantId
    ORDER BY ra.startDate
    """)
  List<Room> findRoomAvailabilitiesByTenantIdAndDeletedAtIsNull(@Param("tenantId") Long tenantId);

  @Query("""
      SELECT r.name FROM Room r
      WHERE r.property.id = :propertyId
      AND r.deletedAt IS NULL
      """)
  List<String> findAllRoomNamesByPropertyId(Long propertyId);

  @Query("""
      SELECT r FROM Room r
      WHERE r.property.id = :propertyId
      AND r.deletedAt IS NULL
      AND EXISTS (
        SELECT 1
        FROM RoomAvailability ra
        WHERE ra.room.id = r.id
        AND :date BETWEEN ra.startDate AND ra.endDate
        AND ra.isAvailable = false
      )
      """)
  List<Room> findUnavailableRoomsByPropertyIdAndDate(Long propertyId, LocalDate date);

  @Query("""
      SELECT new com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO(
        r.property.id,
        p.name,
        r.id,
        r.name,
        r.imageUrl,
        r.capacity,
        r.description,
        r.basePrice,
        psr.adjustmentType,
        psr.adjustmentRate,
        CASE
          WHEN EXISTS (
            SELECT 1 FROM RoomAvailability ra
            WHERE ra.room.id = r.id
              AND :date BETWEEN ra.startDate AND ra.endDate
              AND ra.isAvailable = false
          ) THEN false
          ELSE true
        END
      )
      FROM Room r
      JOIN Property p ON r.property.id = p.id
      LEFT JOIN PeakSeasonRate psr ON p.id = psr.property.id
        AND :date BETWEEN psr.startDate AND psr.endDate
      WHERE r.id = :roomId
        AND p.deletedAt IS NULL
        AND r.deletedAt IS NULL
      ORDER BY r.id
      LIMIT 1
      """)
  RoomPriceRateDTO findRoomRateAndAvailability(@Param("roomId") Long roomId, @Param("date") LocalDate date);

}
