package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

  Optional<Property> findByLocationAndDeletedAtIsNull(Point location);
  Optional<Property> findByIdAndDeletedAtIsNull(Long id);
}
