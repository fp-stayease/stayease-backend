package com.finalproject.stayease.bookings.service;

import com.finalproject.stayease.bookings.dto.BookingItemReqDto;
import com.finalproject.stayease.bookings.dto.BookingReqDto;
import com.finalproject.stayease.bookings.dto.BookingRequestReqDto;
import com.finalproject.stayease.bookings.dto.BookingResDto;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.property.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {
    Booking createBooking(BookingReqDto reqDto, Long userId, Long roomId, Double amount);
    void createBookingItem(BookingItemReqDto reqDto, Booking newBooking, Room room);
    void createBookingRequest(BookingRequestReqDto reqDto, Booking newBooking);
    Booking findById(UUID bookingId);
    BookingResDto getBookingById(UUID bookingId);
    Page<BookingResDto> getUserBookings(Long userId, Pageable pageable);
    Booking updateBooking(UUID bookingId, String bookingStatus);
    Page<BookingResDto> getTenantBookings(Long userId, Pageable pageable);
    void userBookingReminder();
}
