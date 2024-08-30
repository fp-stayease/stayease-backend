package com.finalproject.stayease.bookings.service.impl;

import com.finalproject.stayease.bookings.dto.BookingItemReqDto;
import com.finalproject.stayease.bookings.dto.BookingReqDto;
import com.finalproject.stayease.bookings.dto.BookingRequestReqDto;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.entity.BookingRequest;
import com.finalproject.stayease.bookings.repository.BookingItemRepository;
import com.finalproject.stayease.bookings.repository.BookingRepository;
import com.finalproject.stayease.bookings.repository.BookingRequestRepository;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UsersService usersService;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository, BookingRequestRepository bookingRequestRepository, UsersService usersService) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingRequestRepository = bookingRequestRepository;
        this.usersService = usersService;
    }

    @Override
    @Transactional
    public Booking createBooking(BookingReqDto reqDto, Long userId, Long roomId) {
        Booking newBooking = new Booking();
        // TO DO: Search and validate user
        var user = usersService.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        newBooking.setUser(user);
        newBooking.setTotalPrice(reqDto.getPrice());
        newBooking.setStatus("In progress");

        bookingRepository.save(newBooking);

        createBookingItem(reqDto.getBookingItem(), newBooking, roomId);

        if (reqDto.getBookingRequest() != null) {
            createBookingRequest(reqDto.getBookingRequest(), newBooking);
        }

        return bookingRepository.save(newBooking);
    }

    @Override
    public void createBookingItem(BookingItemReqDto bookingItemDto, Booking newBooking, Long roomId) {
        BookingItem bookingItem = new BookingItem();
        bookingItem.setBooking(newBooking);
        bookingItem.setRoomId(roomId);
        bookingItem.setCheckInDate(bookingItemDto.getCheckInDate());
        bookingItem.setCheckOutDate(bookingItemDto.getCheckOutDate());
        bookingItem.setPrice(bookingItemDto.getPrice());
        bookingItem.setTotalAdults(bookingItemDto.getTotalAdults());
        bookingItem.setTotalChildren(bookingItemDto.getTotalChildren());
        bookingItem.setTotalInfants(bookingItemDto.getTotalInfants());

        bookingItemRepository.save(bookingItem);
    }

    @Override
    public void createBookingRequest(BookingRequestReqDto reqDto, Booking newBooking) {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setBooking(newBooking);
        if (reqDto.getCheckInTime() != null) {
            bookingRequest.setCheckInTime(reqDto.getCheckInTime());
        }
        if (reqDto.getCheckOutTime() != null) {
            bookingRequest.setCheckOutTime(reqDto.getCheckOutTime());
        }
        bookingRequest.setNonSmoking(reqDto.isNonSmoking());
        if (reqDto.getOther() != null) {
            bookingRequest.setOther(reqDto.getOther());
        }
        bookingRequestRepository.save(bookingRequest);
    }

    @Override
    public Booking getBookingDetail(UUID bookingId) {
        return bookingRepository.findById(bookingId).
                orElseThrow(() -> new DataNotFoundException("Booking not found"));
    }

    @Override
    public Page<Booking> getUserBookings(Long userId, Pageable pageable) {
        // TO DO: find and validate user
        var user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));

        return bookingRepository.findByUserId(user.getId(), pageable);
    }

    @Override
    public Booking updateBooking(UUID bookingId, String bookingStatus) {
        Booking booking = getBookingDetail(bookingId);
        booking.setStatus(bookingStatus);
        return bookingRepository.save(booking);
    }
}
