package com.jccv.tuprivadaapp.dto.stripe;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StripeOxxoVoucherResponse {
    private Long paymentId;
    private String voucherUrl;
    private LocalDateTime expiresAt;
    private Long totalToPay; // en centavos
    private String clientSecret;
    private String residentFullName;
    private String paymentReference;
}
