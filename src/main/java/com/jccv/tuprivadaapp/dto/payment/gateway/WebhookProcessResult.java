package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

/**
 * DTO para resultado de procesamiento de webhook
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookProcessResult {
    
    private boolean processed;
    private String eventType;
    private String transactionReference;
    private String message;
    private boolean requiresAction;
    private String actionRequired;
}
