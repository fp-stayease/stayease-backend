package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
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
    """)
  List<Room> findRoomAvailabilitiesByTenantIdAndDeletedAtIsNull(@Param("tenantId") Long tenantId);
}
