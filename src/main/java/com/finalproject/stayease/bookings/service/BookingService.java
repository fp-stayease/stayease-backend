package com.finalproject.stayease.bookings.service;

import com.finalproject.stayease.bookings.dto.BookingItemReqDto;
import com.finalproject.stayease.bookings.dto.BookingReqDto;
import com.finalproject.stayease.bookings.dto.BookingRequestReqDto;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.entity.BookingRequest;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingReqDto reqDto, Long userId);
    void createBookingItem(BookingItemReqDto reqDto, Booking newBooking);
    void createBookingRequest(BookingRequestReqDto reqDto, Booking newBooking);
    Booking getBookingDetail(Long bookingId);
    List<Booking> getUserBookings(Long userId);
    Booking updateBooking(Long bookingId);
    void deleteBooking(Long bookingId);
}
