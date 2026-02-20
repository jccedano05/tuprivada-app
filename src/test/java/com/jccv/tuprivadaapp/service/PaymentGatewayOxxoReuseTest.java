package com.jccv.tuprivadaapp.service;

import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentInitiationRequest;
import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentIntentResponse;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Charge;
import com.jccv.tuprivadaapp.model.Resident;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayAccountService;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayException;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar la lógica de reutilización y expiración de referencias OXXO
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class PaymentGatewayOxxoReuseTest {

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private PaymentGatewayAccountService gatewayAccountService;

    private Payment testPayment;
    private PaymentInitiationRequest oxxoRequest;

    @BeforeEach
    public void setUp() {
        // Configurar datos de prueba
        oxxoRequest = new PaymentInitiationRequest();
        oxxoRequest.setPaymentMethod(PaymentTransaction.PaymentMethod.OXXO);
        oxxoRequest.setEmail("test@example.com");
        oxxoRequest.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);
        oxxoRequest.setExpiresInDays(3);
        oxxoRequest.setIpAddress("192.168.1.1");
    }

    @Test
    @DisplayName("Debe reutilizar referencia OXXO válida cuando existe una pendiente no expirada")
    public void testReuseValidOxxoReference() throws PaymentGatewayException {
        // Arrange: Crear primera referencia OXXO
        oxxoRequest.setPaymentId(1L);
        PaymentIntentResponse firstResponse = paymentGatewayService.initiatePaymentFromExisting(oxxoRequest);
        
        assertNotNull(firstResponse);
        assertNotNull(firstResponse.getTransactionReference());
        String firstTransactionRef = firstResponse.getTransactionReference();
        
        // Act: Intentar crear otra referencia para el mismo payment
        PaymentIntentResponse secondResponse = paymentGatewayService.initiatePaymentFromExisting(oxxoRequest);
        
        // Assert: Debe retornar la misma referencia
        assertNotNull(secondResponse);
        assertEquals(firstTransactionRef, secondResponse.getTransactionReference(), 
            "Debe reutilizar la referencia OXXO existente");
        assertEquals("Referencia OXXO existente reutilizada", secondResponse.getMessage());
        assertEquals(firstResponse.getReferenceNumber(), secondResponse.getReferenceNumber());
        assertEquals(firstResponse.getBarcodeUrl(), secondResponse.getBarcodeUrl());
    }

    @Test
    @DisplayName("Debe crear nueva referencia OXXO cuando la anterior expiró")
    public void testCreateNewOxxoReferenceWhenExpired() throws PaymentGatewayException {
        // Arrange: Crear primera referencia OXXO
        oxxoRequest.setPaymentId(2L);
        PaymentIntentResponse firstResponse = paymentGatewayService.initiatePaymentFromExisting(oxxoRequest);
        
        assertNotNull(firstResponse);
        String firstTransactionRef = firstResponse.getTransactionReference();
        
        // Simular expiración: modificar la fecha de expiración en la BD
        Optional<PaymentTransaction> txOpt = transactionRepository.findByTransactionReference(firstTransactionRef);
        assertTrue(txOpt.isPresent());
        
        PaymentTransaction tx = txOpt.get();
        tx.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expiró hace 1 día
        transactionRepository.save(tx);
        
        // Act: Intentar crear otra referencia para el mismo payment
        PaymentIntentResponse secondResponse = paymentGatewayService.initiatePaymentFromExisting(oxxoRequest);
        
        // Assert: Debe crear una nueva referencia
        assertNotNull(secondResponse);
        assertNotEquals(firstTransactionRef, secondResponse.getTransactionReference(), 
            "Debe crear una nueva referencia cuando la anterior expiró");
        
        // Verificar que la primera transacción fue marcada como EXPIRED
        Optional<PaymentTransaction> expiredTxOpt = transactionRepository.findByTransactionReference(firstTransactionRef);
        assertTrue(expiredTxOpt.isPresent());
        assertEquals(PaymentTransaction.TransactionStatus.EXPIRED, expiredTxOpt.get().getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el pago ya fue completado")
    public void testThrowExceptionWhenPaymentAlreadyPaid() {
        // Arrange: Crear un payment que ya está pagado
        oxxoRequest.setPaymentId(3L);
        
        // Simular que el payment ya está pagado
        // (esto depende de tu implementación de PaymentService)
        
        // Act & Assert
        PaymentGatewayException exception = assertThrows(
            PaymentGatewayException.class,
            () -> paymentGatewayService.initiatePaymentFromExisting(oxxoRequest)
        );
        
        assertEquals("PAYMENT_ALREADY_PAID", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("ya ha sido completado"));
    }

    @Test
    @DisplayName("Debe permitir múltiples referencias OXXO si las anteriores fallaron")
    public void testAllowNewOxxoReferenceWhenPreviousFailed() throws PaymentGatewayException {
        // Arrange: Crear primera referencia OXXO
        oxxoRequest.setPaymentId(4L);
        PaymentIntentResponse firstResponse = paymentGatewayService.initiatePaymentFromExisting(oxxoRequest);
        
        assertNotNull(firstResponse);
        String firstTransactionRef = firstResponse.getTransactionReference();
        
        // Simular fallo: marcar la transacción como FAILED
        Optional<PaymentTransaction> txOpt = transactionRepository.findByTransactionReference(firstTransactionRef);
        assertTrue(txOpt.isPresent());
        
        PaymentTransaction tx = txOpt.get();
        tx.setStatus(PaymentTransaction.TransactionStatus.FAILED);
        transactionRepository.save(tx);
        
        // Act: Intentar crear otra referencia para el mismo payment
        PaymentIntentResponse secondResponse = paymentGatewayService.initiatePaymentFromExisting(oxxoRequest);
        
        // Assert: Debe crear una nueva referencia porque la anterior falló
        assertNotNull(secondResponse);
        assertNotEquals(firstTransactionRef, secondResponse.getTransactionReference(), 
            "Debe crear una nueva referencia cuando la anterior falló");
    }

    @Test
    @DisplayName("No debe permitir duplicar transacciones pendientes para pagos con tarjeta")
    public void testPreventDuplicateCardTransactions() throws PaymentGatewayException {
        // Arrange: Crear primera transacción con tarjeta
        PaymentInitiationRequest cardRequest = new PaymentInitiationRequest();
        cardRequest.setPaymentId(5L);
        cardRequest.setPaymentMethod(PaymentTransaction.PaymentMethod.CARD);
        cardRequest.setEmail("test@example.com");
        cardRequest.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);
        cardRequest.setCardToken("tok_test_visa_4242");
        
        PaymentIntentResponse firstResponse = paymentGatewayService.initiatePaymentFromExisting(cardRequest);
        assertNotNull(firstResponse);
        
        // Act & Assert: Intentar crear otra transacción debe fallar
        PaymentGatewayException exception = assertThrows(
            PaymentGatewayException.class,
            () -> paymentGatewayService.initiatePaymentFromExisting(cardRequest)
        );
        
        assertEquals("PENDING_TRANSACTION_EXISTS", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Ya existe una orden de pago pendiente"));
    }
}
