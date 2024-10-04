package com.finalproject.stayease.transactions.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.helpers.HtmlDataMap;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.midtrans.dto.BankTransferDTO;
import com.finalproject.stayease.midtrans.dto.MidtransReqDTO;
import com.finalproject.stayease.midtrans.dto.TransactionDetailDTO;
import com.finalproject.stayease.midtrans.service.MidtransService;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.property.service.RoomAvailabilityService;
import com.finalproject.stayease.transactions.dto.request.NotificationReqDTO;
import com.finalproject.stayease.transactions.dto.request.TransactionReqDTO;
import com.finalproject.stayease.transactions.dto.TransactionDTO;
import com.finalproject.stayease.transactions.service.TransactionService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log
public class TransactionServiceImpl implements TransactionService {
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final MidtransService midtransService;
    private final UsersService usersService;
    private final MailService mailService;
    private final HtmlDataMap htmlDataMap;
    private final RoomAvailabilityService roomAvailabilityService;

    @Value("${midtrans.secret.key}")
    private String midtransSecret;

    // User Transaction Section

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionReqDTO reqDto, Long userId, Long roomId) {
        Booking newBooking = bookingService.createBooking(reqDto.getBooking(), userId, roomId, reqDto.getAmount());

        if (Objects.equals(reqDto.getPaymentMethod(), "bank_transfer")){
            var transactionDetail = new TransactionDetailDTO();
            transactionDetail.setOrder_id(String.valueOf(newBooking.getId()));
            transactionDetail.setGross_amount(reqDto.getAmount());

            var bankTransfer = new BankTransferDTO();
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

            Payment newPayment = paymentService.createPayment(
                    reqDto.getAmount(),
                    reqDto.getPaymentMethod(),
                    newBooking,
                    PaymentStatus.fromString(status),
                    String.valueOf(vaObject.get("va_number")),
                    String.valueOf(vaObject.get("bank"))
            );

            return toResDto(
                    newBooking.getId(),
                    newBooking.getStatus(),
                    newPayment.getPaymentMethod(),
                    newPayment.getPaymentStatus(),
                    newPayment.getPaymentExpirationAt()
            );
        }

        Payment newPayment = paymentService.createPayment(
                reqDto.getAmount(), reqDto.getPaymentMethod(), newBooking, PaymentStatus.PENDING
        );

        return toResDto(newBooking.getId(), newBooking.getStatus(), newPayment.getPaymentMethod(), newPayment.getPaymentStatus(), newPayment.getPaymentExpirationAt());
    }

    @Override
    @Transactional
    public TransactionDTO notificationHandler(NotificationReqDTO reqDto) throws IOException, InterruptedException, MessagingException {
        List<String> failedTransactionStatuses = List.of("expire", "cancel", "deny", "failure");
        Payment updatedPayment;
        Booking updatedBooking;

        Payment payment = paymentService.findPaymentByBookingId(UUID.fromString(reqDto.getOrder_id()));
        Booking booking = bookingService.findById(UUID.fromString(reqDto.getOrder_id()));

        log.info("Incoming notif from -> " + reqDto.getOrder_id());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sandbox.midtrans.com/v2/" + reqDto.getOrder_id() + "/status"))
                .header("accept", "application/json")
                .header("authorization", midtransSecret)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        var responseBody = response.body();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String transactionStatus = jsonNode.get("transaction_status").asText();

        if (failedTransactionStatuses.contains(transactionStatus)) {
            updatedPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.fromString(transactionStatus));
            if (Objects.equals(transactionStatus, "expire")) {
                updatedBooking = bookingService.updateBooking(booking.getId(), BookingStatus.EXPIRED);
            } else {
                updatedBooking = bookingService.updateBooking(booking.getId(), BookingStatus.FAILED);
            }

            var bookingItems = updatedBooking.getBookingItems();
            for (BookingItem bookingItem : bookingItems) {
                roomAvailabilityService.removeUnavailability(
                        bookingItem.getRoom().getId(), updatedBooking.getCheckInDate(), updatedBooking.getCheckOutDate()
                );
            }
            return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
        }

        Users user = booking.getUser();
        if (Objects.equals(transactionStatus, "settlement")) {
            var data = htmlDataMap.dataGenerator(booking);
            String message = """
                    Dear Guest,\s
                    Thank you for trusting us to be your trusted accommodation finder and booking!\s
                    We have received your payment and you have complete your transaction.\s
                    Enjoy your trip!\s
                    Sincerely,\s
                    Stay Ease Admin""";
            updatedPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.SETTLEMENT);
            updatedBooking = bookingService.updateBooking(booking.getId(), BookingStatus.PAYMENT_COMPLETE);
            mailService.sendMailWithPdf(user.getEmail(), "Booking Invoice", "booking-invoice.html", data, message);

            return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
        }

        updatedPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.fromString(transactionStatus));
        updatedBooking = bookingService.updateBooking(booking.getId(), BookingStatus.fromString(transactionStatus));

        return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
    }

    @Override
    @Transactional
    public TransactionDTO userCancelTransaction(UUID bookingId, Long userId) throws MessagingException {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);

        if (!Objects.equals(booking.getUser().getId(), userId)) {
            throw new RuntimeException("This is not your booking");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("You have paid your booking, you cannot cancel this transaction");
        }

        var cancelledBooking = bookingService.updateBooking(bookingId, BookingStatus.CANCELLED);
        var cancelledPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.CANCEL);

        var bookingItems = cancelledBooking.getBookingItems();
        for (BookingItem bookingItem : bookingItems) {
            roomAvailabilityService.removeUnavailability(bookingItem.getRoom().getId(), cancelledBooking.getCheckInDate(), cancelledBooking.getCheckOutDate());
        }

        String message = """
                    We sorry to hear you cancelled the bookings due to some reasons.\s
                    We will wait for your another stay plan with us.\s
                    Thank you for trusting StayEase!\s
                    \s
                    Best regards,\s
                    Stay Ease Customer Service
                """;

        mailService.sendMimeMessage(booking.getUser().getEmail(), "Booking Cancellation", message);

        return toResDto(cancelledBooking.getId(), cancelledBooking.getStatus(), cancelledPayment.getPaymentMethod(), cancelledPayment.getPaymentStatus());
    }

    // Tenant Transaction Section

    @Override
    @Transactional
    public TransactionDTO tenantRejectTransaction(UUID bookingId, Long userId) throws MessagingException {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);
        Users user = usersService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(booking.getTenant().getUser().getId(), user.getId())) {
            throw new RuntimeException("This booking does not belong to this tenant");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("This booking already has a payment proof");
        }

        var rejectedBooking = bookingService.updateBooking(bookingId, BookingStatus.IN_PROGRESS);
        var rejectedPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.PENDING);
        paymentService.tenantRejectPayment(payment.getId());

        String message = """
                    Tenant has rejected your bookings.\s
                    Please check your payment detail and complete the transaction.\s
                    \s
                    Best regards,\s
                    Stay Ease Customer Service
                """;

        mailService.sendMimeMessage(booking.getUser().getEmail(), "Booking Payment Rejected", message);

        return toResDto(rejectedBooking.getId(), rejectedBooking.getStatus(), rejectedPayment.getPaymentMethod(), rejectedPayment.getPaymentStatus());
    }

    @Transactional
    @Scheduled(cron = "*/30 * * * * *")
    public void autoCancelTransaction() throws MessagingException {
        var payments = paymentService.findExpiredPendingPayment();

        if (payments.isEmpty()) {
            log.info("No expired payment found");
            return;
        }

        for (Payment payment : payments) {
            Booking booking = payment.getBooking();
            var cancelledPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.EXPIRE);
            var cancelledBooking = bookingService.updateBooking(booking.getId(),BookingStatus.EXPIRED);

            var bookingItems = cancelledBooking.getBookingItems();
            for (BookingItem bookingItem : bookingItems) {
                roomAvailabilityService.removeUnavailability(bookingItem.getRoom().getId(), cancelledBooking.getCheckInDate(), cancelledBooking.getCheckOutDate().minusDays(1));
            }

            String message = """
                        Your booking has expired.\s
                        If you still want to booked the properties, please book it again and don't forget to complete the payment.\s
                        \s
                        Best regards,\s
                        Stay Ease Customer Service
                    """;

            mailService.sendMimeMessage(booking.getUser().getEmail(), "Booking expired", message);

            log.info("Payment with id -> " + cancelledPayment.getId() + " and booking id " + cancelledBooking.getId() + " has been cancelled due to expiration time.");
        }
    }

    @Override
    @Transactional
    public TransactionDTO approveTransaction(UUID bookingId) throws MessagingException {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(booking.getId());
        if (payment.getPaymentProof() == null) {
            throw new RuntimeException("This booking does not have a payment proof, you cannot approve this booking");
        }

        var updatedBooking = bookingService.updateBooking(bookingId, BookingStatus.PAYMENT_COMPLETE);
        var updatedPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.SETTLEMENT);

        String message = """
                    Tenant has approved your bookings.\s
                    Now the things you will do to wait until your days come!\s
                    Thank you for trusting Stay Ease! We will wait for your next stay!\s
                    \s
                    Best regards,\s
                    Stay Ease Customer Service
                """;

        mailService.sendMimeMessage(booking.getUser().getEmail(), "", message);

        return toResDto(updatedBooking.getId(), updatedBooking.getStatus(), updatedPayment.getPaymentMethod(), updatedPayment.getPaymentStatus());
    }

    @Override
    public TransactionDTO tenantCancelTransaction(UUID bookingId, Long userId) throws MessagingException {
        Booking booking = bookingService.findById(bookingId);
        Payment payment = paymentService.findPaymentByBookingId(bookingId);
        Users user = usersService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(booking.getTenant().getUser().getId(), user.getId())) {
            throw new RuntimeException("This booking does not belong to this tenant");
        }
        if (payment.getPaymentProof() != null) {
            throw new RuntimeException("This booking already paid by the user, you cannot cancel this transaction");
        }

        var cancelledBooking = bookingService.updateBooking(bookingId, BookingStatus.CANCELLED);
        var cancelledPayment = paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.CANCEL);

        var bookingItems = cancelledBooking.getBookingItems();
        for (BookingItem bookingItem : bookingItems) {
            roomAvailabilityService.removeUnavailability(bookingItem.getRoom().getId(), cancelledBooking.getCheckInDate(), cancelledBooking.getCheckOutDate());
        }

        String message = """
                    Tenant has rejected your bookings.\s
                    Thank you for trusting Stay Ease!\s
                    Best regards,\s
                    Stay Ease Customer Service
                """;

        mailService.sendMimeMessage(cancelledBooking.getUser().getEmail(), "Booking Cancelled", message);
        return toResDto(cancelledBooking.getId(), cancelledBooking.getStatus(), cancelledPayment.getPaymentMethod(), cancelledPayment.getPaymentStatus());
    }

    private TransactionDTO toResDto(
            UUID bookingId, BookingStatus bookingStatus, String paymentMethod, PaymentStatus paymentStatus, Instant paymentExpiredAt
    ) {
        var response = new TransactionDTO();
        response.setBookingId(bookingId);
        response.setBookingStatus(bookingStatus);
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStatus(paymentStatus);
        response.setPaymentExpiredAt(paymentExpiredAt);

        return response;
    }

    private TransactionDTO toResDto(
            UUID bookingId, BookingStatus bookingStatus, String paymentMethod, PaymentStatus paymentStatus
    ) {
        var response = new TransactionDTO();
        response.setBookingId(bookingId);
        response.setBookingStatus(bookingStatus);
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStatus(paymentStatus);

        return response;
    }

    private MidtransReqDTO toMidtransReqDto(TransactionDetailDTO transactionDetail, BankTransferDTO bankTransfer, String paymentMethod) {
        MidtransReqDTO midtransReqDto = new MidtransReqDTO();
        midtransReqDto.setTransaction_details(transactionDetail);
        midtransReqDto.setBank_transfer(bankTransfer);

        midtransReqDto.setPayment_type(paymentMethod);

        return midtransReqDto;
    }
}
