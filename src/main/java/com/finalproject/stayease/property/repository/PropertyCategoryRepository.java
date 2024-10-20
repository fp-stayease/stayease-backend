package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.PropertyCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyCategoryRepository extends JpaRepository<PropertyCategory, Long> {

  Optional<PropertyCategory> findByNameIgnoreCaseAndDeletedAtIsNull(String name);
  Optional<PropertyCategory> findByIdAndDeletedAtIsNull(Long categoryId);

}
