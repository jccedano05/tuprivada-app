package com.jccv.tuprivadaapp.service.payment.gateway;

import com.jccv.tuprivadaapp.dto.payment.gateway.*;

public interface PaymentGatewayService {

    PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws PaymentGatewayException;

    PaymentConfirmationResponse confirmPayment(String transactionReference, PaymentConfirmationRequest request) throws PaymentGatewayException;

    TransactionStatusResponse getTransactionStatus(String transactionReference) throws PaymentGatewayException;

    RefundResponse refundPayment(String transactionReference, RefundRequest request) throws PaymentGatewayException;

    WebhookProcessResult processWebhook(String provider, String payload, String signature) throws PaymentGatewayException;
    
    PaymentIntentResponse initiatePaymentFromExisting(PaymentInitiationRequest request) throws PaymentGatewayException;
}
