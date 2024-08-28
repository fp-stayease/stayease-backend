package com.finalproject.stayease.transactions.service;

import com.finalproject.stayease.transactions.dto.NotificationReqDto;
import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.dto.TransactionResDto;

import java.io.IOException;

public interface TransactionService {
    TransactionResDto createTransaction(TransactionReqDto reqDto, Long userId);
    void notificationHandler(NotificationReqDto reqDto) throws IOException, InterruptedException;
}
