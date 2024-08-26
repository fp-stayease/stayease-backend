package com.finalproject.stayease.midtrans.service.impl;

import com.finalproject.stayease.midtrans.dto.MidtransReqDto;
import com.finalproject.stayease.midtrans.service.MidtransService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransCoreApi;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class MidtransServiceImpl implements MidtransService {

    private final MidtransCoreApi midtransCoreApi;

    public MidtransServiceImpl(MidtransCoreApi midtransCoreApi) {
        this.midtransCoreApi = midtransCoreApi;
    }

    @Override
    public JSONObject createTransaction(MidtransReqDto reqDto) {
        try {
            Map<String, Object> transactionDetails = new HashMap<>();
            transactionDetails.put("order_id", reqDto.getTransaction_details().getOrder_id());
            transactionDetails.put("gross_amount", reqDto.getTransaction_details().getGross_amount());

            Map<String, Object> bankTransferDetails = new HashMap<>();
            bankTransferDetails.put("bank", reqDto.getBank_transfer().getBank());

            Map<String, Object> transactionRequest = new HashMap<>();
            transactionRequest.put("payment_type", reqDto.getPayment_type());
            transactionRequest.put("transaction_details", transactionDetails);

            if (Objects.equals(reqDto.getPayment_type(), "bank_transfer") && reqDto.getBank_transfer() != null) {
                transactionRequest.put("bank_transfer", bankTransferDetails);
            }

            return midtransCoreApi.chargeTransaction(transactionRequest);

        } catch (MidtransError e) {
            throw new RuntimeException("Error creating Midtrans transaction -> ", e);
        }
    }
}
