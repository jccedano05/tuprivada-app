package com.jccv.tuprivadaapp.service.stripe;

import com.jccv.tuprivadaapp.dto.stripe.StripePaymentIntentResponse;
import com.jccv.tuprivadaapp.dto.stripe.StripePaymentRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;

@Service
public interface StripePaymentsService {
    StripePaymentIntentResponse createPaymentCardIntent(StripePaymentRequest request) throws StripeException;

    public void handlePaymentIntentSucceeded(PaymentIntent paymentIntent);
}
