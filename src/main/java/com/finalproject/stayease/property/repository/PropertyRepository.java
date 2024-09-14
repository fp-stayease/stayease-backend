package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

  Optional<Property> findByLocationAndDeletedAtIsNull(Point location);

  Optional<Property> findByIdAndDeletedAtIsNull(Long id);

  List<Property> findByTenantAndDeletedAtIsNull(Users tenant);

  @Query("SELECT DISTINCT p.city FROM Property p")
  List<String> findDistinctCities();

  @Query("""
      SELECT imageUrl FROM Property WHERE imageUrl IS NOT NULL AND deletedAt IS NULL
      UNION
      SELECT imageUrl FROM Room WHERE imageUrl IS NOT NULL AND deletedAt IS NULL
      """)
  List<String> findAllPropertyRoomImageUrls();

  @Query("""
      SELECT new com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO(
        p.id,
        p.name,
        r.id,
        r.name,
        r.imageUrl,
        r.capacity,
        r.description,
        r.basePrice,
        psr.adjustmentType,
        psr.rateAdjustment,
        true
        )
      FROM Property p
      JOIN Room r ON p.id = r.property.id
      LEFT JOIN PeakSeasonRate psr ON p.id = psr.property.id
      AND :date BETWEEN psr.startDate AND psr.endDate
      WHERE p.id = :propertyId
      AND p.deletedAt IS NULL
      AND r.deletedAt IS NULL
      AND NOT EXISTS (
        SELECT 1
        FROM RoomAvailability ra
        WHERE ra.room.id = r.id
        AND :date BETWEEN ra.startDate AND ra.endDate
        AND ra.isAvailable = false
      )
      ORDER BY r.basePrice asc
      """)
  List<RoomPriceRateDTO> findAvailableRoomRates(Long propertyId, LocalDate date);

  @Query("""
          SELECT NEW com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO(
              p.id, p.name, p.description, p.imageUrl, p.city, pc.name,
              p.longitude, p.latitude,
              (SELECT MIN(r.basePrice)
               FROM Room r
               WHERE r.property = p
               AND r.deletedAt IS NULL
               AND NOT EXISTS (
                   SELECT 1
                   FROM RoomAvailability ra
                   WHERE ra.room = r
                   AND ra.startDate <= :endDate
                   AND ra.endDate >= :startDate
                   AND ra.isAvailable = false
               )
               ),
              NULL
              )
          FROM Property p
          JOIN p.category pc
          WHERE p.deletedAt IS NULL
          AND (:city IS NULL OR p.city = :city)
          AND (:categoryId IS NULL OR pc.id = :categoryId)
          AND (:searchTerm IS NULL OR CAST(LOWER(p.name) AS string) LIKE CONCAT('%', CAST(:searchTerm AS string), '%'))
          AND EXISTS (
              SELECT 1
              FROM Room r
              WHERE r.property = p
              AND r.deletedAt IS NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM RoomAvailability ra
                  WHERE ra.room = r
                  AND ra.startDate <= :endDate
                  AND ra.endDate >= :startDate
                  AND ra.isAvailable = false
              )
          )
      """)
  List<PropertyListingDTO> findAvailableProperties(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("city") String city,
      @Param("categoryId") Long categoryId,
      @Param("searchTerm") String searchTerm
  );

  // Quarantine
//  @Query("""
//    SELECT NEW com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO(
//        p.id, p.name, p.description, p.imageUrl, p.city, pc.name,
//        p.longitude, p.latitude,
//        (SELECT MIN(r.basePrice)
//         FROM Room r
//         WHERE r.property = p
//         AND r.deletedAt IS NULL
//         AND NOT EXISTS (
//             SELECT 1
//             FROM RoomAvailability ra
//             WHERE ra.room = r
//             AND ra.startDate <= :endDate
//             AND ra.endDate >= :startDate
//             AND ra.isAvailable = false
//         )),
//        CASE
//            WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL
//            THEN ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
//            ELSE NULL
//        END
//    )
//    FROM Property p
//    JOIN p.category pc
//    WHERE p.deletedAt IS NULL
//    AND (:city IS NULL OR p.city = :city)
//    AND (:categoryId IS NULL OR pc.id = :categoryId)
//    AND (:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
//    AND EXISTS (
//        SELECT 1
//        FROM Room r
//        WHERE r.property = p
//        AND r.deletedAt IS NULL
//        AND NOT EXISTS (
//            SELECT 1
//            FROM RoomAvailability ra
//            WHERE ra.room = r
//            AND ra.startDate <= :endDate
//            AND ra.endDate >= :startDate
//            AND ra.isAvailable = false
//        )
//    )
//    AND (:radius IS NULL OR :latitude IS NULL OR :longitude IS NULL OR
//             function('ST_DWithin', p.location, function('ST_SetSRID', function('ST_MakePoint', :longitude, :latitude), 4326), :radius) = true)
//""")
//  List<PropertyListingDTO> findAvailableProperties(
//      @Param("startDate") LocalDate startDate,
//      @Param("endDate") LocalDate endDate,
//      @Param("city") String city,
//      @Param("categoryId") Long categoryId,
//      @Param("searchTerm") String searchTerm,
//      @Param("longitude") Double longitude,
//      @Param("latitude") Double latitude,
//      @Param("radius") Double radius
//  );
}
