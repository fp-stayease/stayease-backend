package com.finalproject.stayease.transactions.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.transactions.dto.request.NotificationReqDTO;
import com.finalproject.stayease.transactions.dto.request.TransactionReqDTO;
import com.finalproject.stayease.transactions.service.TransactionService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
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
    private final UsersService usersService;

    public TransactionController(TransactionService transactionService, UsersService usersService) {
        this.transactionService = transactionService;
        this.usersService = usersService;
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionReqDTO reqDto, @PathVariable Long roomId) {
        Long userId = usersService.getLoggedUser().getId();
        var response = transactionService.createTransaction(reqDto, userId, roomId);
        return Response.successfulResponse(HttpStatus.OK.value(), "Transaction success", response);
    }

    @PostMapping("/notification-handler")
    public ResponseEntity<?> notificationHandler(@RequestBody NotificationReqDTO reqDto) throws IOException, InterruptedException, MessagingException {
        var response = transactionService.notificationHandler(reqDto);
        return Response.successfulResponse("Updated", response);
    }

    @PutMapping("/user/{bookingId}")
    public ResponseEntity<?> userCancelTransaction(@PathVariable String bookingId) throws MessagingException {
        Long userId = usersService.getLoggedUser().getId();
        var response = transactionService.userCancelTransaction(UUID.fromString(bookingId), userId);

        return Response.successfulResponse("Transaction cancelled by user", response);
    }

    @PutMapping("/tenant/{bookingId}")
    public ResponseEntity<?> tenantRejectTransaction(@PathVariable String bookingId) throws MessagingException {
        Long userId = usersService.getLoggedUser().getId();
        var response = transactionService.tenantRejectTransaction(UUID.fromString(bookingId), userId);

        return Response.successfulResponse("Transaction cancelled by tenant", response);
    }

    @PatchMapping("/tenant/{bookingId}")
    public ResponseEntity<?> approveTransaction(@PathVariable String bookingId) throws MessagingException {
        var response = transactionService.approveTransaction(UUID.fromString(bookingId));
        return Response.successfulResponse("Approved transaction", response);
    }

    @PostMapping("/tenant/{bookingId}")
    public ResponseEntity<?> tenantCancelTransaction(@PathVariable String bookingId) throws MessagingException {
        Long userId = usersService.getLoggedUser().getId();
        var response = transactionService.tenantCancelTransaction(UUID.fromString(bookingId), userId);
        return Response.successfulResponse("Transaction cancelled by tenant", response);
    }
}
