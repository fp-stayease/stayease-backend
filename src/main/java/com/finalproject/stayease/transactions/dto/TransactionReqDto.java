package com.finalproject.stayease.transactions.dto;

import com.finalproject.stayease.bookings.dto.BookingReqDto;
import lombok.Data;

@Data
public class TransactionReqDto {
    BookingReqDto booking;
    Double amount;
    String paymentMethod;
    String bank;
}
