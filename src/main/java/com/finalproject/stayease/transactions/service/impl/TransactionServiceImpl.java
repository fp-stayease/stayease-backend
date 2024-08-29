package com.finalproject.stayease.transactions.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.midtrans.dto.BankTransfer;
import com.finalproject.stayease.midtrans.dto.MidtransReqDto;
import com.finalproject.stayease.midtrans.dto.TransactionDetail;
import com.finalproject.stayease.midtrans.service.MidtransService;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.transactions.dto.NotificationReqDto;
import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.dto.TransactionResDto;
import com.finalproject.stayease.transactions.service.TransactionService;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
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

            if (!Objects.equals(statusCode, "201")) {
                throw new RuntimeException("Midtrans error");
            }

            Payment newPayment = paymentService.createPayment(reqDto.getAmount(), reqDto.getPaymentMethod(), newBooking, status, String.valueOf(vaObject.get("va_number")));

            return toResDto(newBooking.getId(), newBooking.getStatus(), newPayment.getPaymentMethod(), newPayment.getPaymentStatus(), newPayment.getPaymentExpirationAt());
        }

        Payment newPayment = paymentService.createPayment(reqDto.getAmount(), reqDto.getPaymentMethod(), newBooking, "Waiting for payment");

        return toResDto(newBooking.getId(), newBooking.getStatus(), newPayment.getPaymentMethod(), newPayment.getPaymentStatus(), newPayment.getPaymentExpirationAt());
    }

    @Override
    public TransactionResDto notificationHandler(NotificationReqDto reqDto) throws IOException, InterruptedException {
        Payment payment = paymentService.findPaymentByBookingId(UUID.fromString(reqDto.getOrder_id()));
        Booking booking = bookingService.getBookingDetail(UUID.fromString(reqDto.getOrder_id()));

        log.info("Incoming notif from -> " + reqDto.getOrder_id());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sandbox.midtrans.com/v2/" + reqDto.getOrder_id() + "/status"))
                .header("accept", "application/json")
                .header("authorization", "Basic U0ItTWlkLXNlcnZlci1xSzlJVjh6WUF4NERWcU9jeDY2R2wtVl86UnVreXkwMTA2IQ==")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        var responseBody = response.body();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String transactionStatus = jsonNode.get("transaction_status").asText();

        paymentService.updatePaymentStatus(payment.getId(), transactionStatus);
        bookingService.updateBooking(booking.getId(), "paid");

        return toResDto(booking.getId(), booking.getStatus(), payment.getPaymentMethod(), payment.getPaymentStatus());
    }

    @Override
    public TransactionResDto userCancelTransaction(UUID bookingId, Long userId) {
        Booking booking = bookingService.getBookingDetail(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);

        if (!Objects.equals(booking.getUserId(), userId)) {
            throw new RuntimeException("This is not your booking");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("You have paid your booking, you cannot cancel this transaction");
        }

        bookingService.updateBooking(bookingId, "cancelled");
        paymentService.updatePaymentStatus(payment.getId(), "cancelled");

        return toResDto(booking.getId(), booking.getStatus(), payment.getPaymentMethod(), payment.getPaymentStatus());
    }

    @Override
    public TransactionResDto tenantCancelTransaction(UUID bookingId, Long tenantId) {
        Booking booking = bookingService.getBookingDetail(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);

        if (!Objects.equals(booking.getTenantId(), tenantId)) {
            throw new RuntimeException("This booking does not belong to this tenant");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("This booking already has a payment proof");
        }

        bookingService.updateBooking(bookingId, "cancelled");
        paymentService.updatePaymentStatus(payment.getId(), "cancelled");

        return toResDto(booking.getId(), booking.getStatus(), payment.getPaymentMethod(), payment.getPaymentStatus());
    }

    public TransactionResDto toResDto(
            UUID bookingId, String bookingStatus, String paymentMethod, String paymentStatus, Instant paymentExpiredAt
    ) {
        var response = new TransactionResDto();
        response.setBookingId(bookingId);
        response.setBookingStatus(bookingStatus);
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStaus(paymentStatus);
        response.setPaymentExpiredAt(paymentExpiredAt);

        return response;
    }

    public TransactionResDto toResDto(
            UUID bookingId, String bookingStatus, String paymentMethod, String paymentStatus
    ) {
        var response = new TransactionResDto();
        response.setBookingId(bookingId);
        response.setBookingStatus(bookingStatus);
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStaus(paymentStatus);

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
