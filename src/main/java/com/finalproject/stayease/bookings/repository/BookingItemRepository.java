package com.finalproject.stayease.bookings.repository;

import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.reports.dto.properties.PopularRoomDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    @Query("""
        SELECT new com.finalproject.stayease.reports.dto.properties.PopularRoomDTO(
            bi.room.name,
            bi.room.property.name,
            bi.room.imageUrl,
            COUNT(bi)
        ) FROM BookingItem bi
        WHERE bi.booking.tenant.id = :tenantId
        AND bi.booking.payment.paymentStatus = 'SETTLEMENT'
        GROUP BY bi.room.name, bi.room.property.name, bi.room.imageUrl
        ORDER BY COUNT(bi) DESC
    """)
    List<PopularRoomDTO> findMostBookedRoomByTenantId(@Param("tenantId") Long tenantId);
}
