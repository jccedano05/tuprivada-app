package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para solicitar la creación de una intención de pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentIntentRequest {

    @NotNull(message = "El proveedor de pago es obligatorio")
    private PaymentGatewayAccount.PaymentProvider provider;

    @NotNull(message = "El ID del pago es obligatorio")
    private Long paymentId;
    
    @NotNull(message = "El ID del condominio es obligatorio")
    private Long condominiumId;
    
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "MXN";
    
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentTransaction.PaymentMethod paymentMethod;
    
    // Datos del pagador/cliente
    @NotNull(message = "El email del cliente es obligatorio")
    private String customerEmail;
    
    @NotNull(message = "El nombre del cliente es obligatorio")
    private String customerName;
    
    private String customerPhone;
    
    // Alias para compatibilidad con código existente
    public String getPayerEmail() { return customerEmail; }
    public String getPayerName() { return customerName; }
    public String getPayerPhone() { return customerPhone; }
    
    // Referencia bancaria personal del usuario
    private String bankPersonalReference;
    
    // Dirección del residente
    private String street;
    private String houseNumber;
    private String interiorNumber;
    
    // Descripción del pago
    private String description;
    
    // Token de tarjeta (para pagos con tarjeta)
    private String cardToken;
    
    // URLs de confirmación
    private String successUrl;
    private String webhookUrl;
    
    // IP del cliente para prevención de fraude
    private String ipAddress;
    
    // Alias para compatibilidad
    public String getClientIp() { return ipAddress; }
    
    // User agent del navegador
    private String userAgent;
    
    // Metadatos adicionales
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    // Referencia interna para correlación
    private String transactionReference;
    
    // Device fingerprint para prevención de fraude
    private String deviceFingerprint;
}
