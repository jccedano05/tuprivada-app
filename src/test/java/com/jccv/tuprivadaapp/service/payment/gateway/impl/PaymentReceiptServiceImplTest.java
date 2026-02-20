package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PaymentReceiptServiceImpl.
 * Verifica la generación correcta de comprobantes PDF.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReceiptService - Tests de Generación de PDFs")
class PaymentReceiptServiceImplTest {
    
    @Mock
    private PaymentTransactionRepository transactionRepository;
    
    @InjectMocks
    private PaymentReceiptServiceImpl receiptService;
    
    private PaymentTransaction successfulTransaction;
    private PaymentTransaction pendingTransaction;
    private PaymentTransaction failedTransaction;
    
    @BeforeEach
    void setUp() {
        // Crear usuario y residente
        User user = User.builder()
                .id(1L)
                .email("resident@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();
        
        Resident resident = new Resident();
        resident.setId(1L);
        resident.setUser(user);
        
        // Crear cargo
        Charge charge = Charge.builder()
                .id(1L)
                .description("Mantenimiento mensual")
                .titleTypePayment("Mantenimiento")
                .amount(1500.00)
                .dueDate(LocalDateTime.now().plusDays(10))
                .build();
        
        // Crear payment
        Payment payment = Payment.builder()
                .id(100L)
                .resident(resident)
                .charge(charge)
                .isPaid(true)
                .build();
        
        // Transacción exitosa
        successfulTransaction = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("TXN-SUCCESS-001")
                .gatewayTransactionId("ord_success_123")
                .payment(payment)
                .status(PaymentTransaction.TransactionStatus.SUCCEEDED)
                .paymentMethod(PaymentTransaction.PaymentMethod.OXXO)
                .amount(new BigDecimal("1500.00"))
                .currency("MXN")
                .gatewayFee(new BigDecimal("12.50"))
                .platformFee(new BigDecimal("5.00"))
                .taxAmount(new BigDecimal("2.80"))
                .netAmount(new BigDecimal("1479.70"))
                .createdAt(LocalDateTime.now().minusDays(2))
                .confirmedAt(LocalDateTime.now().minusDays(1))
                .referenceNumber("98123456789012")
                .build();
        
        // Transacción pendiente
        pendingTransaction = PaymentTransaction.builder()
                .id(2L)
                .transactionReference("TXN-PENDING-001")
                .payment(payment)
                .status(PaymentTransaction.TransactionStatus.PENDING)
                .paymentMethod(PaymentTransaction.PaymentMethod.OXXO)
                .amount(new BigDecimal("1500.00"))
                .currency("MXN")
                .createdAt(LocalDateTime.now())
                .build();
        
        // Transacción fallida
        failedTransaction = PaymentTransaction.builder()
                .id(3L)
                .transactionReference("TXN-FAILED-001")
                .payment(payment)
                .status(PaymentTransaction.TransactionStatus.FAILED)
                .paymentMethod(PaymentTransaction.PaymentMethod.CARD)
                .amount(new BigDecimal("1500.00"))
                .currency("MXN")
                .createdAt(LocalDateTime.now())
                .errorCode("card_declined")
                .errorMessage("Tarjeta declinada")
                .build();
    }
    
    @Test
    @DisplayName("Genera PDF correctamente para transacción exitosa")
    void testGenerateReceiptForSuccessfulTransaction() {
        // Given
        when(transactionRepository.findByTransactionReference("TXN-SUCCESS-001"))
                .thenReturn(Optional.of(successfulTransaction));
        
        // When
        byte[] pdfBytes = receiptService.generateReceipt("TXN-SUCCESS-001");
        
        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        
        // Verificar que comienza con header PDF
        String pdfHeader = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
        assertEquals("%PDF", pdfHeader, "Debe ser un archivo PDF válido");
        
        verify(transactionRepository).findByTransactionReference("TXN-SUCCESS-001");
    }
    
    @Test
    @DisplayName("Genera PDF correctamente para transacción CAPTURED")
    void testGenerateReceiptForCapturedTransaction() {
        // Given
        successfulTransaction.setStatus(PaymentTransaction.TransactionStatus.CAPTURED);
        when(transactionRepository.findByTransactionReference("TXN-SUCCESS-001"))
                .thenReturn(Optional.of(successfulTransaction));
        
        // When
        byte[] pdfBytes = receiptService.generateReceipt("TXN-SUCCESS-001");
        
        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(transactionRepository).findByTransactionReference("TXN-SUCCESS-001");
    }
    
    @Test
    @DisplayName("Lanza BadRequestException para transacción PENDING")
    void testThrowsExceptionForPendingTransaction() {
        // Given
        when(transactionRepository.findByTransactionReference("TXN-PENDING-001"))
                .thenReturn(Optional.of(pendingTransaction));
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            receiptService.generateReceipt("TXN-PENDING-001"));
        
        assertTrue(exception.getMessage().contains("Solo se pueden generar comprobantes para pagos exitosos"));
        assertTrue(exception.getMessage().contains("PENDING"));
        verify(transactionRepository).findByTransactionReference("TXN-PENDING-001");
    }
    
    @Test
    @DisplayName("Lanza BadRequestException para transacción FAILED")
    void testThrowsExceptionForFailedTransaction() {
        // Given
        when(transactionRepository.findByTransactionReference("TXN-FAILED-001"))
                .thenReturn(Optional.of(failedTransaction));
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            receiptService.generateReceipt("TXN-FAILED-001"));
        
        assertTrue(exception.getMessage().contains("Solo se pueden generar comprobantes"));
        verify(transactionRepository).findByTransactionReference("TXN-FAILED-001");
    }
    
    @Test
    @DisplayName("Lanza ResourceNotFoundException si la transacción no existe")
    void testThrowsExceptionWhenTransactionNotFound() {
        // Given
        when(transactionRepository.findByTransactionReference(anyString()))
                .thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            receiptService.generateReceipt("TXN-INVALID"));
        
        assertTrue(exception.getMessage().contains("No se encontró la transacción"));
        assertTrue(exception.getMessage().contains("TXN-INVALID"));
        verify(transactionRepository).findByTransactionReference("TXN-INVALID");
    }
    
    @Test
    @DisplayName("Genera PDF incluso sin información de residente")
    void testGenerateReceiptWithoutResidentInfo() {
        // Given
        successfulTransaction.getPayment().setResident(null);
        when(transactionRepository.findByTransactionReference("TXN-SUCCESS-001"))
                .thenReturn(Optional.of(successfulTransaction));
        
        // When
        byte[] pdfBytes = receiptService.generateReceipt("TXN-SUCCESS-001");
        
        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "Debe generar PDF aunque falte información de residente");
        verify(transactionRepository).findByTransactionReference("TXN-SUCCESS-001");
    }
    
    @Test
    @DisplayName("Genera PDF incluso sin información de cargo")
    void testGenerateReceiptWithoutChargeInfo() {
        // Given
        successfulTransaction.getPayment().setCharge(null);
        when(transactionRepository.findByTransactionReference("TXN-SUCCESS-001"))
                .thenReturn(Optional.of(successfulTransaction));
        
        // When
        byte[] pdfBytes = receiptService.generateReceipt("TXN-SUCCESS-001");
        
        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "Debe generar PDF aunque falte información de cargo");
        verify(transactionRepository).findByTransactionReference("TXN-SUCCESS-001");
    }
    
    @Test
    @DisplayName("Genera PDF sin comisiones si son null")
    void testGenerateReceiptWithoutFees() {
        // Given
        successfulTransaction.setGatewayFee(null);
        successfulTransaction.setPlatformFee(null);
        successfulTransaction.setTaxAmount(null);
        when(transactionRepository.findByTransactionReference("TXN-SUCCESS-001"))
                .thenReturn(Optional.of(successfulTransaction));
        
        // When
        byte[] pdfBytes = receiptService.generateReceipt("TXN-SUCCESS-001");
        
        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "Debe generar PDF aunque no haya comisiones");
        verify(transactionRepository).findByTransactionReference("TXN-SUCCESS-001");
    }
    
    @Test
    @DisplayName("Genera PDF para pago con tarjeta sin referencia OXXO")
    void testGenerateReceiptForCardPayment() {
        // Given
        successfulTransaction.setPaymentMethod(PaymentTransaction.PaymentMethod.CARD);
        successfulTransaction.setReferenceNumber(null);
        when(transactionRepository.findByTransactionReference("TXN-SUCCESS-001"))
                .thenReturn(Optional.of(successfulTransaction));
        
        // When
        byte[] pdfBytes = receiptService.generateReceipt("TXN-SUCCESS-001");
        
        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(transactionRepository).findByTransactionReference("TXN-SUCCESS-001");
    }
}
