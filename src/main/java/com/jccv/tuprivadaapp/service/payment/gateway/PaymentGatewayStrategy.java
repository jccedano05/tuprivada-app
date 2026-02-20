package com.jccv.tuprivadaapp.service.payment.gateway;

import com.jccv.tuprivadaapp.dto.payment.gateway.*;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;

/**
 * Interfaz Strategy para definir las operaciones que debe implementar cada pasarela de pago.
 * Patrón Strategy para permitir múltiples implementaciones de pasarelas.
 */
public interface PaymentGatewayStrategy {
    
    /**
     * Crea una intención de pago en la pasarela
     */
    PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws PaymentGatewayException;
    
    /**
     * Confirma un pago pendiente
     */
    PaymentConfirmationResponse confirmPayment(String transactionReference, PaymentConfirmationRequest request) throws PaymentGatewayException;
    
    /**
     * Cancela una transacción
     */
    void cancelPayment(String transactionReference) throws PaymentGatewayException;
    
    /**
     * Procesa un reembolso
     */
    RefundResponse refundPayment(String transactionReference, RefundRequest request) throws PaymentGatewayException;
    
    /**
     * Obtiene el estado actual de una transacción
     */
    TransactionStatusResponse getTransactionStatus(String transactionReference) throws PaymentGatewayException;
    
    /**
     * Procesa un webhook de la pasarela
     */
    WebhookProcessResult processWebhook(String payload, String signature) throws PaymentGatewayException;
    
    /**
     * Crea una cuenta conectada en la pasarela
     */
    AccountCreationResponse createConnectedAccount(AccountCreationRequest request) throws PaymentGatewayException;
    
    /**
     * Obtiene información de una cuenta conectada
     */
    AccountInfoResponse getAccountInfo(String accountId) throws PaymentGatewayException;
    
    /**
     * Genera un enlace de onboarding para completar el registro
     */
    String generateOnboardingLink(String accountId, String returnUrl, String refreshUrl) throws PaymentGatewayException;
    
    /**
     * Valida si la cuenta está lista para recibir pagos
     */
    boolean isAccountReady(String accountId) throws PaymentGatewayException;
    
    /**
     * Obtiene el proveedor de esta estrategia
     */
    PaymentGatewayAccount.PaymentProvider getProvider();
    
    /**
     * Valida la configuración de la pasarela
     */
    boolean validateConfiguration();
    
    /**
     * Genera referencia para pago en efectivo (OXXO, etc)
     */
    CashPaymentReference generateCashPaymentReference(CashPaymentRequest request) throws PaymentGatewayException;
    
    /**
     * Calcula las comisiones para una transacción
     */
    FeeCalculation calculateFees(PaymentIntentRequest request);
}
