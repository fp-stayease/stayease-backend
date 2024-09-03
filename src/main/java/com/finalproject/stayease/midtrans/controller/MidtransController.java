package com.finalproject.stayease.midtrans.controller;

import com.finalproject.stayease.midtrans.dto.MidtransReqDto;
import com.finalproject.stayease.midtrans.service.MidtransService;
import com.finalproject.stayease.responses.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class MidtransController {
    private final MidtransService paymentService;

    public MidtransController(MidtransService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody MidtransReqDto reqDto) {
        var response = paymentService.createTransaction(reqDto);
        return Response.successfulResponse(HttpStatus.CREATED.value(), "Transaction created successfully", response.toMap());
    }
}
