package com.jccv.tuprivadaapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentInitiationRequest;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el sistema de pagos con Conekta
 * Valida el flujo completo de pagos y manejo de errores
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class PaymentGatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    public void setUp() throws Exception {
        // Simular autenticación y obtener token
        authToken = "Bearer test-token";
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testInitiateOxxoPayment_Success() throws Exception {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(1L);
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.OXXO);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);
        request.setExpiresInDays(3);
        request.setIpAddress("192.168.1.1");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(in(new String[]{"PENDING", "PROCESSING"})))
                .andExpect(jsonPath("$.referenceNumber").exists())
                .andExpect(jsonPath("$.transactionReference").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testInitiateCardPaymentWithoutToken_ReturnsCheckoutUrl() throws Exception {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(1L);
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.CARD);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);
        request.setIpAddress("192.168.1.1");
        // No incluimos cardToken para simular pago sin tokenización

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUIRES_ACTION"))
                .andExpect(jsonPath("$.paymentUrl").exists())
                .andExpect(jsonPath("$.requiresAction").value(true))
                .andExpect(jsonPath("$.nextAction").value("Completar pago en página segura de Conekta"))
                .andExpect(jsonPath("$.transactionReference").exists());
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testInitiateCardPaymentWithToken_ProcessesPayment() throws Exception {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(1L);
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.CARD);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);
        request.setCardToken("tok_test_visa_4242");
        request.setIpAddress("192.168.1.1");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(in(new String[]{"PROCESSING", "PENDING", "SUCCEEDED"})))
                .andExpect(jsonPath("$.transactionReference").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testGetTransactionStatus_Success() throws Exception {
        // Primero crear una transacción
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(1L);
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.OXXO);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);

        MvcResult result = mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        String transactionRef = objectMapper.readTree(responseContent).get("transactionReference").asText();

        // Consultar estado de la transacción
        mockMvc.perform(get("/api/v1/payment-gateway/payment/{transactionReference}/status", transactionRef)
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value(transactionRef))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testInitiatePayment_WithoutPaymentId_ReturnsBadRequest() throws Exception {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        // No establecemos paymentId
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.OXXO);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testInitiatePayment_NonExistentPayment_ReturnsNotFound() throws Exception {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(999999L); // ID inexistente
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.OXXO);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testInitiatePayment_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Arrange
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(1L);
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.OXXO);
        request.setEmail("test@example.com");
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);

        // Act & Assert - Sin token de autenticación
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testWebhookProcessing_OrderPaid() throws Exception {
        // Arrange
        String webhookPayload = """
            {
                "id": "evt_test_001",
                "type": "order.paid",
                "created_at": 1706286000,
                "livemode": false,
                "data": {
                    "object": {
                        "id": "ord_test_001",
                        "payment_status": "paid",
                        "amount": 50000,
                        "currency": "MXN",
                        "metadata": {
                            "transactionReference": "TXN-TEST-001"
                        }
                    }
                }
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/webhooks/conekta")
                .header("X-Conekta-Signature", "test-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("order.paid"))
                .andExpect(jsonPath("$.processed").exists());
    }

    @Test
    @WithMockUser(username = "resident@test.com", roles = {"RESIDENT"})
    public void testPaymentFlow_ErrorHandling_ReturnsInternalServerError() throws Exception {
        // Arrange - Request que causará un error interno
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        request.setPaymentId(-1L); // ID inválido que causará error
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.CARD);
        request.setEmail("invalid-email"); // Email inválido
        request.setProvider(PaymentGatewayAccount.PaymentProvider.CONEKTA);

        // Act & Assert - Debe retornar 500, no 401
        mockMvc.perform(post("/api/v1/payment-gateway/initiate")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value(not("AUTHENTICATION_FAILED")))
                .andExpect(jsonPath("$.message").exists());
    }
}
