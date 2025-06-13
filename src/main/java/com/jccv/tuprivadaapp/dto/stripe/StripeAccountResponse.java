package com.jccv.tuprivadaapp.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StripeAccountResponse {
    private String id;
    private String email;
    private boolean chargesEnabled;
}