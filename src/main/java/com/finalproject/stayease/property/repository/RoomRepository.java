package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

  Optional<Room> findByNameIgnoreCaseAndDeletedAtIsNull(String roomName);
  Optional<Room> findByIdAndDeletedAtIsNull(Long id);
  List<Room> findAllByPropertyAndDeletedAtIsNull(Property propertyId);

  @Query("""
      SELECT r.name FROM Room r
      WHERE r.property.id = :propertyId
      AND r.deletedAt IS NULL
      """)
  List<String> findAllRoomNamesByPropertyId(Long propertyId);

}
