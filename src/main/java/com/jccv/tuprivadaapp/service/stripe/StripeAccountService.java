package com.jccv.tuprivadaapp.service.stripe;

import com.jccv.tuprivadaapp.dto.stripe.CreateAccountRequest;
import com.jccv.tuprivadaapp.dto.stripe.StripeAccountResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;

public interface StripeAccountService {
    StripeAccountResponse createAccount(CreateAccountRequest dto);
    String generateAccountLink(String accountId);
}
