package com.tiffin_sathi.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EsewaSignature {

    private EsewaSignature() {} // prevent instantiation

    public static String generate(String secretKey, String totalAmount, String transactionUuid, String productCode) throws Exception {
        String data = "total_amount=" + totalAmount +
                      ",transaction_uuid=" + transactionUuid +
                      ",product_code=" + productCode;

        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        hmac.init(keySpec);

        byte[] hash = hmac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
