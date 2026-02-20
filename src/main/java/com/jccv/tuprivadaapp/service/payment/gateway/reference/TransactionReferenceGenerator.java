package com.jccv.tuprivadaapp.service.payment.gateway.reference;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Genera referencias únicas para transacciones, asegurando trazabilidad.
 */
@Component
public class TransactionReferenceGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String randomSuffix = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        return "TPAY-" + timestamp + "-" + randomSuffix;
    }
}
