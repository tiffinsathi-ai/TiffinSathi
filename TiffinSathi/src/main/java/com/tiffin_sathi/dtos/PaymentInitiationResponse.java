package com.tiffin_sathi.dtos;

import java.util.Map;

public class PaymentInitiationResponse {
    private String paymentId;
    private String paymentMethod;
    private String paymentUrl;
    private Map<String, Object> paymentData;
    private String message;

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public Map<String, Object> getPaymentData() { return paymentData; }
    public void setPaymentData(Map<String, Object> paymentData) { this.paymentData = paymentData; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}