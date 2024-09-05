package com.finalproject.stayease.transactions.service;

import com.finalproject.stayease.transactions.dto.NotificationReqDto;
import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.dto.TransactionResDto;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.UUID;

public interface TransactionService {
    TransactionResDto createTransaction(TransactionReqDto reqDto, Long userId, Long roomId);
    TransactionResDto notificationHandler(NotificationReqDto reqDto) throws IOException, InterruptedException, MessagingException;
    TransactionResDto userCancelTransaction(UUID bookingId, Long userId);
    TransactionResDto tenantCancelTransaction(UUID bookingId, Long userId);
    void autoCancelTransaction();
    TransactionResDto approveTransaction(UUID bookingId);
}
