package com.finalproject.stayease.transactions.service.impl;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.midtrans.dto.BankTransfer;
import com.finalproject.stayease.midtrans.dto.MidtransReqDto;
import com.finalproject.stayease.midtrans.dto.TransactionDetail;
import com.finalproject.stayease.midtrans.service.MidtransService;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.dto.TransactionResDto;
import com.finalproject.stayease.transactions.service.TransactionService;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@Log
public class TransactionServiceImpl implements TransactionService {
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final MidtransService midtransService;

    public TransactionServiceImpl(BookingService bookingService, PaymentService paymentService, MidtransService midtransService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.midtransService = midtransService;
    }

    @Override
    @Transactional
    public TransactionResDto createTransaction(TransactionReqDto reqDto, Long userId) {
        Booking newBooking = bookingService.createBooking(reqDto.getBooking(), userId);

        if (Objects.equals(reqDto.getPaymentMethod(), "bank_transfer")){
            var transactionDetail = new TransactionDetail();
            transactionDetail.setOrder_id(String.valueOf(newBooking.getId()));
            transactionDetail.setGross_amount(reqDto.getAmount());

            var bankTransfer = new BankTransfer();
            bankTransfer.setBank(reqDto.getBank());

            var midtransReqDto = toMidtransReqDto(transactionDetail, bankTransfer, reqDto.getPaymentMethod());

            var midtrans = midtransService.createTransaction(midtransReqDto);
            var status = (String) midtrans.get("transaction_status");
            var statusCode = (String) midtrans.get("status_code");
            var vaNumber = (JSONArray) midtrans.get("va_numbers");
            var vaObject = (JSONObject) vaNumber.get(0);
            log.info("Va number: " + vaObject.get("va_number"));
            log.info("bank: " + vaObject.get("bank"));

            if (!Objects.equals(statusCode, "201")) {
                throw new RuntimeException("Midtrans error");
            }

            Payment newPayment = paymentService.createPayment(reqDto.getAmount(), reqDto.getPaymentMethod(), newBooking, status);

            return toResDto(newBooking.getId(), newBooking.getStatus(), newPayment.getPaymentMethod(), newPayment.getPaymentStatus(), newPayment.getPaymentExpirationAt());
        }

        Payment newPayment = paymentService.createPayment(reqDto.getAmount(), reqDto.getPaymentMethod(), newBooking, "Waiting for payment");

        return toResDto(newBooking.getId(), newBooking.getStatus(), newPayment.getPaymentMethod(), newPayment.getPaymentStatus(), newPayment.getPaymentExpirationAt());
    }

    public TransactionResDto toResDto(
            UUID bookingId, String bookingStatus, String paymentMethod, String paymentStatus, Instant paymentExpiredAt)
    {
        var response = new TransactionResDto();
        response.setBookingId(bookingId);
        response.setBookingStatus(bookingStatus);
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStaus(paymentStatus);
        response.setPaymentExpiredAt(paymentExpiredAt);

        return response;
    }

    public MidtransReqDto toMidtransReqDto(TransactionDetail transactionDetail, BankTransfer bankTransfer, String paymentMethod) {
        MidtransReqDto midtransReqDto = new MidtransReqDto();
        midtransReqDto.setTransaction_details(transactionDetail);
        midtransReqDto.setBank_transfer(bankTransfer);

        midtransReqDto.setPayment_type(paymentMethod);

        return midtransReqDto;
    }
}
