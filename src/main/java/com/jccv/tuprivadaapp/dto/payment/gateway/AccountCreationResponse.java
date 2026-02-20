package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

/**
 * DTO para respuesta de creación de cuenta conectada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreationResponse {
    
    private String accountId;
    private String status;
    private String onboardingUrl;
    private boolean requiresVerification;
    private String message;
}
