package com.finalproject.stayease.transactions.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.mail.service.MailService;
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
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Book;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log
public class TransactionServiceImpl implements TransactionService {
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final MidtransService midtransService;
    private final UsersService usersService;
    private final MailService mailService;

    @Override
    @Transactional
    public TransactionResDto createTransaction(TransactionReqDto reqDto, Long userId, Long roomId) {
        Booking newBooking = bookingService.createBooking(reqDto.getBooking(), userId, roomId);

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

        Payment newPayment = paymentService.createPayment(reqDto.getAmount(), reqDto.getPaymentMethod(), newBooking, "pending");

        return toResDto(newBooking.getId(), newBooking.getStatus(), newPayment.getPaymentMethod(), newPayment.getPaymentStatus(), newPayment.getPaymentExpirationAt());
    }

    @Override
    @Transactional
    public TransactionResDto notificationHandler(NotificationReqDto reqDto) throws IOException, InterruptedException, MessagingException {
        Payment payment = paymentService.findPaymentByBookingId(UUID.fromString(reqDto.getOrder_id()));
        Booking booking = bookingService.findById(UUID.fromString(reqDto.getOrder_id()));

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

        Payment updatedPayment;
        Booking updatedBooking;

        if (Objects.equals(transactionStatus, "expire")) {
            updatedPayment = paymentService.updatePaymentStatus(payment.getId(), transactionStatus);
            updatedBooking = bookingService.updateBooking(booking.getId(), "cancelled");
            return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
        }

        updatedPayment = paymentService.updatePaymentStatus(payment.getId(), transactionStatus);
        updatedBooking = bookingService.updateBooking(booking.getId(), "paid");

        Users user = booking.getUser();
        var bookingItems = booking.getBookingItems();
        Map<String, String> data = new HashMap<>();
        data.put("userEmail", user.getEmail());
        data.put("userName", user.getFirstName() + " " + user.getLastName());
        data.put("checkInDate", booking.getCheckInDate().toString());
        data.put("checkOutDate", booking.getCheckOutDate().toString());
        data.put("bookingId", booking.getId().toString());

        StringBuilder rooms = new StringBuilder();
        for (BookingItem bookingItem : bookingItems) {
            rooms.append("<div class=\"detail-row\">")
                    .append("<span>Room:</span>")
                    .append("<span>").append(bookingItem.getRoomId()).append("</span>")
                    .append("</div>");
        }
        data.put("roomName", rooms.toString());

        mailService.sendMailWithPdf(user.getEmail(), "Booking Invoice", "booking-invoice.html", data);

        return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
    }

    @Override
    @Transactional
    public TransactionResDto userCancelTransaction(UUID bookingId, Long userId) {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);

        if (!Objects.equals(booking.getUser().getId(), userId)) {
            throw new RuntimeException("This is not your booking");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("You have paid your booking, you cannot cancel this transaction");
        }

        var cancelledBooking = bookingService.updateBooking(bookingId, "cancelled");
        var cancelledPayment = paymentService.updatePaymentStatus(payment.getId(), "cancelled");

        return toResDto(cancelledBooking.getId(), cancelledBooking.getStatus(), cancelledPayment.getPaymentMethod(), cancelledPayment.getPaymentStatus());
    }

    @Override
    @Transactional
    public TransactionResDto tenantCancelTransaction(UUID bookingId, Long userId) {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);
        Users user = usersService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(booking.getTenant().getUser().getId(), user.getId())) {
            throw new RuntimeException("This booking does not belong to this tenant");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("This booking already has a payment proof");
        }

        var cancelledBooking = bookingService.updateBooking(bookingId, "pending");
        var cancelledPayment = paymentService.updatePaymentStatus(payment.getId(), "pending");

        return toResDto(cancelledBooking.getId(), cancelledBooking.getStatus(), cancelledPayment.getPaymentMethod(), cancelledPayment.getPaymentStatus());
    }

    @Override
    @Transactional
    @Scheduled(cron = "*/30 * * * * *")
    public void autoCancelTransaction() {
        var payments = paymentService.findExpiredPendingPayment();

        for (Payment payment : payments) {
            var updatedPayment = paymentService.updatePaymentStatus(payment.getId(), "expired");
            var updatedBooking = bookingService.updateBooking(payment.getBooking().getId(),"expired");

            log.info("Payment with id -> " + updatedPayment.getId() + " and booking id " + updatedBooking.getId() + " has been cancelled due to expiration time.");
        }
    }

    @Override
    @Transactional
    public TransactionResDto approveTransaction(UUID bookingId) {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(booking.getId());
        if (payment.getPaymentProof() == null) {
            throw new RuntimeException("This booking does not have a payment proof, you cannot approve this booking");
        }

        var updatedBooking = bookingService.updateBooking(bookingId, "paid");
        var updatedPayment = paymentService.updatePaymentStatus(payment.getId(), "paid");

        return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
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
