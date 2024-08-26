package com.finalproject.stayease.transactions.service;

import com.finalproject.stayease.transactions.dto.TransactionReqDto;
import com.finalproject.stayease.transactions.dto.TransactionResDto;

public interface TransactionService {
    TransactionResDto createTransaction(TransactionReqDto reqDto, Long userId);
}
