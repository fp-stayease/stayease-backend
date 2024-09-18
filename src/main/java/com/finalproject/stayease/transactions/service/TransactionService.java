package com.finalproject.stayease.transactions.service;

import com.finalproject.stayease.transactions.dto.request.NotificationReqDTO;
import com.finalproject.stayease.transactions.dto.request.TransactionReqDTO;
import com.finalproject.stayease.transactions.dto.TransactionDTO;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.UUID;

public interface TransactionService {
    TransactionDTO createTransaction(TransactionReqDTO reqDto, Long userId, Long roomId);
    TransactionDTO notificationHandler(NotificationReqDTO reqDto) throws IOException, InterruptedException, MessagingException;
    TransactionDTO userCancelTransaction(UUID bookingId, Long userId);
    TransactionDTO tenantRejectTransaction(UUID bookingId, Long userId);
    void autoCancelTransaction();
    TransactionDTO approveTransaction(UUID bookingId);
}
