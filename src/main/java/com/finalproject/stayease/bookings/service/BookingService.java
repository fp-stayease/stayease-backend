package com.finalproject.stayease.bookings.service;

import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.bookings.entity.dto.request.BookingItemReqDTO;
import com.finalproject.stayease.bookings.entity.dto.request.BookingReqDTO;
import com.finalproject.stayease.bookings.entity.dto.request.BookingRequestReqDTO;
import com.finalproject.stayease.bookings.entity.dto.BookingDTO;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.reports.dto.properties.DailySummaryDTO;
import com.finalproject.stayease.reports.dto.properties.PopularRoomDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.Month;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    Booking createBooking(BookingReqDTO reqDto, Long userId, Long roomId, Double amount);
    Booking findById(UUID bookingId);
    BookingDTO getBookingById(UUID bookingId);
    Page<BookingDTO> getUserBookings(Long userId, String search, Pageable pageable);
    Booking updateBooking(UUID bookingId, BookingStatus bookingStatus);
    List<BookingDTO> getTenantBookings(Long userId);
    Long countCompletedBookingsByTenantId(Long userId, Month month);
    Long countUsersTrxByTenantId(Long userId, Month month);
    List<BookingDTO> findTenantRecentCompletedBookings(Long userId);
    List<DailySummaryDTO> getDailySummaryForMonth(Long userId, Long propertyId, Instant startDate, Instant endDate);
    List<PopularRoomDTO> findMostPopularBookings(Long userId);
    Double getTotalRevenueByMonth(Long userId, Long propertyId, Month month);
    Double getTaxByMonthAndProperty(Long userId, Long propertyId, Month month);
    Double upcomingBookingsByUserId(Long userId);
    Double pastBookingsByUserId(Long userId);
    List<Booking> findFinishedBookings();
}
