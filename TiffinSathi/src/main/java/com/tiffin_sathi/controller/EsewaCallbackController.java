package com.tiffin_sathi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffin_sathi.model.Order;
import com.tiffin_sathi.repository.OrderRepository;
import com.tiffin_sathi.utils.EsewaSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;

@Controller
public class EsewaCallbackController {

    @Value("${esewa.secretKey}")
    private String secretKey;

    @Autowired
    private OrderRepository orderRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/esewa/success")
    public String success(@RequestParam(name = "data", required = false) String base64Data) throws Exception {
        if (base64Data == null) return "redirect:/payment/failure?reason=no_data";

        String json = new String(Base64.getDecoder().decode(base64Data));
        JsonNode obj = mapper.readTree(json);

        String totalAmount = obj.get("total_amount").asText();
        String transactionUuid = obj.get("transaction_uuid").asText();
        String productCode = obj.get("product_code").asText();
        String signature = obj.get("signature").asText();
        String status = obj.get("status").asText();

        String generated = EsewaSignature.generate(secretKey, totalAmount, transactionUuid, productCode);

        if (!generated.equals(signature)) {
            return "redirect:/payment/failure?reason=signature_mismatch";
        }

        // Update order status
        orderRepository.findByTransactionUuid(transactionUuid).ifPresent(order -> {
            order.setStatus("COMPLETE".equals(status) ? "PAID" : "FAILED");
            orderRepository.save(order);
        });

        return "redirect:/payment/" + ("COMPLETE".equals(status) ? "success" : "failure") + "?tx=" + transactionUuid;
    }
}
