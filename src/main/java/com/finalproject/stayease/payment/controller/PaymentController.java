package com.finalproject.stayease.payment.controller;

import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.responses.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment-proof/{bookingId}")
    public ResponseEntity<?> uploadPayment(@RequestBody MultipartFile file, @PathVariable String bookingId) throws IOException {
        var response = paymentService.uploadPaymentProof(file, UUID.fromString(bookingId));
        return Response.successfulResponse(HttpStatus.CREATED.value(), "Payment proof uploaded", response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getPaymentByBookingId(@PathVariable String bookingId) {
        var response = paymentService.findPaymentByBookingId(UUID.fromString(bookingId)).toResDto();
        return Response.successfulResponse(HttpStatus.OK.value(), "Payment found", response);
    }
}
