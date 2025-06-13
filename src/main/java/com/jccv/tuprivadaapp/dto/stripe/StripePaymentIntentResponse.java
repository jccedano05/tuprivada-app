package com.jccv.tuprivadaapp.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripePaymentIntentResponse {

    private String clientSecret;
}
