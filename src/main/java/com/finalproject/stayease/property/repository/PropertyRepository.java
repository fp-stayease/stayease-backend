package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.users.entity.Users;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

  Optional<Property> findByLocationAndDeletedAtIsNull(Point location);
  Optional<Property> findByIdAndDeletedAtIsNull(Long id);
  List<Property> findByTenantAndDeletedAtIsNull(Users tenant);

  @Query("SELECT DISTINCT p.city FROM Property p")
  List<String> findDistinctCities();
}
