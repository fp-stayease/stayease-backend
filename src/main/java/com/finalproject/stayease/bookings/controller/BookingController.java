package com.finalproject.stayease.bookings.controller;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.helpers.ExtractToken;
import com.finalproject.stayease.responses.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final JwtService jwtService;
    private final ExtractToken extractToken;

    public BookingController(BookingService bookingService, JwtService jwtService, ExtractToken extractToken) {
        this.bookingService = bookingService;
        this.jwtService = jwtService;
        this.extractToken = extractToken;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(required = false) String search,
            HttpServletRequest request
    ) {
        Long userId = (Long) jwtService.extractClaimsFromToken(extractToken.extractTokenFromRequest(request)).get("userId");
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        var bookings = bookingService.getUserBookings(userId, search, pageable);
        return Response.successfulResponse("User booking list fetched", bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetail(@PathVariable UUID bookingId) {
        var response = bookingService.getBookingById(bookingId);
        return Response.successfulResponse("Booking detail fetched", response);
    }

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenantBookings(
            HttpServletRequest request
    ) {
        Long userId = (Long) jwtService.extractClaimsFromToken(extractToken.extractTokenFromRequest(request)).get("userId");
//        Long userId = 18L;

        var bookings = bookingService.getTenantBookings(userId);
        return Response.successfulResponse("User booking list fetched", bookings);
    }
}
