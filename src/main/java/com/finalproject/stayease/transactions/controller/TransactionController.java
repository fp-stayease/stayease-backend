package com.finalproject.stayease.transactions.controller;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.helpers.ExtractToken;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.transactions.dto.NotificationReqDto;
import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.service.TransactionService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Log
public class TransactionController {
    private final TransactionService transactionService;
    private final JwtService jwtService;
    private final ExtractToken extractToken;

    public TransactionController(TransactionService transactionService, JwtService jwtService, ExtractToken extractToken) {
        this.transactionService = transactionService;
        this.jwtService = jwtService;
        this.extractToken = extractToken;
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionReqDto reqDto, @PathVariable Long roomId, HttpServletRequest request) {
        Long userId = (Long) jwtService.extractClaimsFromToken(extractToken.extractTokenFromRequest(request)).get("userId");
        var response = transactionService.createTransaction(reqDto, userId, roomId);
        return Response.successfulResponse(HttpStatus.OK.value(), "Transaction success", response);
    }

    @PostMapping("/notification-handler")
    public ResponseEntity<?> notificationHandler(@RequestBody NotificationReqDto reqDto) throws IOException, InterruptedException, MessagingException {
        var response = transactionService.notificationHandler(reqDto);
        return Response.successfulResponse("Updated", response);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> tenantCancelTransaction(@PathVariable String bookingId, HttpServletRequest request) {
        Long userId = (Long) jwtService.extractClaimsFromToken(extractToken.extractTokenFromRequest(request)).get("userId");
        var response = transactionService.tenantCancelTransaction(UUID.fromString(bookingId), userId);

        return Response.successfulResponse("Transaction cancelled by tenant", response);
    }

    @PutMapping("/user/{bookingId}")
    public ResponseEntity<?> userCancelTransaction(@PathVariable String bookingId, HttpServletRequest request) {
        Long userId = (Long) jwtService.extractClaimsFromToken(extractToken.extractTokenFromRequest(request)).get("userId");
        var response = transactionService.userCancelTransaction(UUID.fromString(bookingId), userId);

        return Response.successfulResponse("Transaction cancelled by user", response);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<?> approveTransaction(@PathVariable String bookingId) {
        var response = transactionService.approveTransaction(UUID.fromString(bookingId));
        return Response.successfulResponse("Approved transaction", response);
    }
}
