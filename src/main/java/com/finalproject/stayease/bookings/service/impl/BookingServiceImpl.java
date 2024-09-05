package com.finalproject.stayease.bookings.service.impl;

import com.finalproject.stayease.bookings.dto.BookingItemReqDto;
import com.finalproject.stayease.bookings.dto.BookingReqDto;
import com.finalproject.stayease.bookings.dto.BookingRequestReqDto;
import com.finalproject.stayease.bookings.dto.BookingResDto;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.entity.BookingRequest;
import com.finalproject.stayease.bookings.repository.BookingItemRepository;
import com.finalproject.stayease.bookings.repository.BookingRepository;
import com.finalproject.stayease.bookings.repository.BookingRequestRepository;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UsersService usersService;
    private final TenantInfoService tenantInfoService;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository, BookingRequestRepository bookingRequestRepository, UsersService usersService, TenantInfoService tenantInfoService) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingRequestRepository = bookingRequestRepository;
        this.usersService = usersService;
        this.tenantInfoService = tenantInfoService;
    }

    @Override
    @Transactional
    public Booking createBooking(BookingReqDto reqDto, Long userId, Long roomId) {
        Booking newBooking = new Booking();
        var user = usersService.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        // TO DO: Find room and get property ID
        // TO DO: Find property using ID obtained from room and get tenant ID
        // TO DO: Find tenant using tenant ID obtained from property

        newBooking.setUser(user);
        newBooking.setTotalPrice(reqDto.getPrice());
        newBooking.setStatus("In progress");
        newBooking.setCheckInDate(reqDto.getCheckInDate());
        newBooking.setCheckOutDate(reqDto.getCheckOutDate());
        newBooking.setTotalAdults(reqDto.getTotalAdults());
        newBooking.setTotalChildren(reqDto.getTotalChildren());
        newBooking.setTotalInfants(reqDto.getTotalInfants());
//        newBooking.setTenant();

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
    public Booking findById(UUID bookingId) {
        return bookingRepository.findById(bookingId).
                orElseThrow(() -> new DataNotFoundException("Booking not found"));
    }

    @Override
    public BookingResDto getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));
        return booking.toResDto();
    }

    @Override
    public Page<BookingResDto> getUserBookings(Long userId, Pageable pageable) {
        // TO DO: find and validate user
        var user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));

        return bookingRepository.findByUserIdAndStatusNotExpired(user.getId(), pageable).map(Booking::toResDto);
    }

    @Override
    public Booking updateBooking(UUID bookingId, String bookingStatus) {
        Booking booking = findById(bookingId);
        booking.setStatus(bookingStatus);
        return bookingRepository.save(booking);
    }

    @Override
    public Page<BookingResDto> getTenantBookings(Long userId, Pageable pageable) {
        Users user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        TenantInfoResDto tenant = tenantInfoService.findTenantByUserId(user.getId());

        return bookingRepository.findByTenantId(tenant.getId(), pageable).map(Booking::toResDto);
    }
}
