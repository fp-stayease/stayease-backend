package com.finalproject.stayease.midtrans.service;

import com.finalproject.stayease.midtrans.dto.MidtransReqDTO;
import org.json.JSONObject;

public interface MidtransService {
    JSONObject createTransaction(MidtransReqDTO reqDto);
}
