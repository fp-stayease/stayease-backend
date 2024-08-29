package com.finalproject.stayease.transactions.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.transactions.dto.NotificationReqDto;
import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody TransactionReqDto reqDto) {
        Long userId = 1L;
        return Response.successfulResponse(HttpStatus.OK.value(), "Transaction success", transactionService.createTransaction(reqDto, userId));
    }

    @PostMapping("/notification-handler")
    public ResponseEntity<?> notificationHandler(@RequestBody NotificationReqDto reqDto) throws IOException, InterruptedException {
        var response = transactionService.notificationHandler(reqDto);
        return Response.successfulResponse("Updated", response);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> tenantCancelTransaction(@PathVariable String bookingId) {
        Long tenantId = 1L;
        transactionService.tenantCancelTransaction(UUID.fromString(bookingId), tenantId);

        return Response.successfulResponse("Transaction cancelled by tenant");
    }

    @PutMapping("/user/{bookingId}")
    public ResponseEntity<?> userCancelTransaction(@PathVariable String bookingId) {
        Long userId = 1L;
        transactionService.userCancelTransaction(UUID.fromString(bookingId), userId);

        return Response.successfulResponse("Transaction cancelled by user");
    }
}
