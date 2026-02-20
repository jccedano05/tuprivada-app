package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

import java.util.Map;

/**
 * DTO para respuesta de información de cuenta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoResponse {
    
    private String accountId;
    private String businessName;
    private String email;
    private String status;
    private boolean chargesEnabled;
    private boolean payoutsEnabled;
    private Map<String, Object> capabilities;
    private String currency;
    private String country;
}
