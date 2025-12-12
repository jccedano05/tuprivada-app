package com.jccv.tuprivadaapp.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripePaymentRequest {


    private Long paymentId;
    private String paymentMethodType;
    private String email;
}
