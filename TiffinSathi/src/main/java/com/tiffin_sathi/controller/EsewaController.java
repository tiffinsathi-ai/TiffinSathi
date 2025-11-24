package com.tiffin_sathi.controller;

import com.tiffin_sathi.model.Order;
import com.tiffin_sathi.repository.OrderRepository;
import com.tiffin_sathi.utils.EsewaSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/esewa")
public class EsewaController {

    @Value("${esewa.uatAction}")
    private String actionUrl;

    @Value("${esewa.secretKey}")
    private String secretKey;

    @Value("${esewa.productCode}")
    private String productCode;

    @Value("${esewa.successUrl}")
    private String successUrl;

    @Value("${esewa.failureUrl}")
    private String failureUrl;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, String> body) throws Exception {
        double amount = Double.parseDouble(body.getOrDefault("amount", "0"));
        double tax = Double.parseDouble(body.getOrDefault("tax_amount", "0"));
        double service = Double.parseDouble(body.getOrDefault("product_service_charge", "0"));
        double delivery = Double.parseDouble(body.getOrDefault("product_delivery_charge", "0"));

        double total = amount + tax + service + delivery;
        String totalAmount = String.valueOf((int) Math.round(total));
        String transactionUuid = UUID.randomUUID().toString();

        String signature = EsewaSignature.generate(secretKey, totalAmount, transactionUuid, productCode);

        // Save order in DB
        Order order = new Order();
        order.setTransactionUuid(transactionUuid);
        order.setAmount(total);
        order.setStatus("PENDING");
        orderRepository.save(order);


        Map<String, Object> formFields = new HashMap<>();
        formFields.put("total_amount", totalAmount);
        formFields.put("transaction_uuid", transactionUuid);
        formFields.put("product_code", productCode);
        formFields.put("signature", signature);
        formFields.put("success_url", successUrl);
        formFields.put("failure_url", failureUrl);

        Map<String, Object> resp = new HashMap<>();
        resp.put("actionUrl", actionUrl);
        resp.put("formFields", formFields);

        return ResponseEntity.ok(resp);
    }
}
