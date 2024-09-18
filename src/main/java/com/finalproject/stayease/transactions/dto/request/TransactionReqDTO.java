package com.finalproject.stayease.transactions.dto.request;

import com.finalproject.stayease.bookings.entity.dto.request.BookingReqDTO;
import lombok.Data;

@Data
public class TransactionReqDTO {
    BookingReqDTO booking;
    Double amount;
    String paymentMethod;
    String bank;
}
