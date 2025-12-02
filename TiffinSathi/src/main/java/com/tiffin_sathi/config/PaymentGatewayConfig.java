package com.tiffin_sathi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentGatewayConfig {

    @Value("${esewa.merchant.code}")
    private String esewaMerchantCode;

    @Value("${esewa.secret.key}")
    private String esewaSecretKey;

    @Value("${esewa.base.url}")
    private String esewaBaseUrl;

    @Value("${khalti.secret.key}")
    private String khaltiSecretKey;

    @Value("${khalti.base.url}")
    private String khaltiBaseUrl;

    @Value("${khalti.public.key}")
    private String khaltiPublicKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Value("${esewa.success.redirect.url}")
    private String esewaSuccessUrl;

    @Value("${esewa.failure.redirect.url}")
    private String esewaFailureUrl;

    // Getters
    public String getEsewaMerchantCode() { return esewaMerchantCode; }
    public String getEsewaSecretKey() { return esewaSecretKey; }
    public String getEsewaBaseUrl() { return esewaBaseUrl; }
    public String getKhaltiSecretKey() { return khaltiSecretKey; }
    public String getKhaltiBaseUrl() { return khaltiBaseUrl; }
    public String getKhaltiPublicKey() { return khaltiPublicKey; }
    public String getFrontendBaseUrl() { return frontendBaseUrl; }
    public String getEsewaSuccessUrl() { return esewaSuccessUrl; }
    public String getEsewaFailureUrl() { return esewaFailureUrl; }
}