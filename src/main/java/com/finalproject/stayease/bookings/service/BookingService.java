package com.finalproject.stayease.bookings.service;

import com.finalproject.stayease.bookings.entity.dto.request.BookingItemReqDTO;
import com.finalproject.stayease.bookings.entity.dto.request.BookingReqDTO;
import com.finalproject.stayease.bookings.entity.dto.request.BookingRequestReqDTO;
import com.finalproject.stayease.bookings.entity.dto.BookingDTO;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.property.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    Booking createBooking(BookingReqDTO reqDto, Long userId, Long roomId, Double amount);
    void createBookingItem(BookingItemReqDTO reqDto, Booking newBooking, Room room);
    void createBookingRequest(BookingRequestReqDTO reqDto, Booking newBooking);
    Booking findById(UUID bookingId);
    BookingDTO getBookingById(UUID bookingId);
    Page<BookingDTO> getUserBookings(Long userId, String search, Pageable pageable);
    Booking updateBooking(UUID bookingId, String bookingStatus);
    List<BookingDTO> getTenantBookings(Long userId);
    void userBookingReminder();
}
