package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta para cuenta de pasarela de pago.
 * No expone datos sensibles (API keys encriptadas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayAccountResponse {

    private Long id;
    private Long condominiumId;
    private String condominiumName;
    private PaymentGatewayAccount.PaymentProvider provider;
    private String accountId;
    private String accountName;
    private String accountEmail;
    
    /**
     * API Key enmascarada para visualización segura (ej: "key_***xyz").
     */
    private String apiKeyMasked;
    
    private boolean isActive;
    private boolean isVerified;
    private PaymentGatewayAccount.OnboardingStatus onboardingStatus;
    
    private Double commissionPercentage;
    private Double fixedFee;
    
    /**
     * Metadata pública (sin datos sensibles).
     */
    private Map<String, String> publicMetadata;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;
}
