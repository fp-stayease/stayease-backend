package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Room;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

  Optional<Room> findByNameIgnoreCaseAndDeletedAtIsNull(String roomName);
  Optional<Room> findByIdAndDeletedAtIsNull(Long id);

}
