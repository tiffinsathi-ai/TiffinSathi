package com.tiffin_sathi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EsewaStatusService {

    @Value("${esewa.uatStatusCheck}")
    private String statusUrl;

    public String check(String productCode, String transactionUuid, String totalAmount) {
        String url = statusUrl +
                     "?product_code=" + productCode +
                     "&total_amount=" + totalAmount +
                     "&transaction_uuid=" + transactionUuid;

        RestTemplate rest = new RestTemplate();
        return rest.getForObject(url, String.class);
    }
}
