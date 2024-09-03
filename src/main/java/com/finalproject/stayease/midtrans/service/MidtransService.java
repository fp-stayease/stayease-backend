package com.finalproject.stayease.midtrans.service;

import com.finalproject.stayease.midtrans.dto.MidtransReqDto;
import org.json.JSONObject;

public interface MidtransService {
    JSONObject createTransaction(MidtransReqDto reqDto);
}
