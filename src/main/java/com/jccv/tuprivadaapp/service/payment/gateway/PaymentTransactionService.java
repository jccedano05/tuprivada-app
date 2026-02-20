package com.jccv.tuprivadaapp.service.payment.gateway;

import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentIntentRequest;
import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentIntentResponse;
import com.jccv.tuprivadaapp.dto.payment.gateway.TransactionDetailResponse;
import com.jccv.tuprivadaapp.dto.payment.gateway.TransactionListItemDTO;
import com.jccv.tuprivadaapp.dto.payment.gateway.TransactionStatusHistoryDTO;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de transacciones de pago.
 * Abstrae la lógica de negocio de las transacciones de pasarela.
 * 
 * @author TuPrivada Development Team
 * @version 2.0
 * @since 2026-01-21
 */
public interface PaymentTransactionService {

    // ==================== MÉTODOS EXISTENTES (NO MODIFICAR) ====================
    
    PaymentTransaction createPendingTransaction(PaymentIntentRequest request,
                                                PaymentGatewayAccount gatewayAccount,
                                                String transactionReference);

    PaymentTransaction updateStatus(String transactionReference,
                                    PaymentTransaction.TransactionStatus status,
                                    String gatewayTransactionId,
                                    String message,
                                    String responseRaw);

    void recordError(String transactionReference, String errorCode, String errorMessage);

    Optional<PaymentTransaction> findByReference(String transactionReference);

    void incrementRetryCount(String transactionReference);

    PaymentTransaction syncWithGatewayResponse(String transactionReference, PaymentIntentResponse response);
    
    String generateTransactionReference();
    
    // ==================== NUEVOS MÉTODOS PARA FASE 2 ====================
    
    /**
     * Obtiene todas las transacciones de un Payment con paginación y filtros opcionales.
     * 
     * @param paymentId ID del Payment
     * @param status Estado opcional para filtrar (puede ser null)
     * @param pageable Configuración de paginación
     * @return Página de transacciones mapeadas a DTO
     */
    Page<TransactionListItemDTO> findByPaymentIdPaginated(
            Long paymentId,
            PaymentTransaction.TransactionStatus status,
            Pageable pageable
    );
    
    /**
     * Obtiene el detalle completo de una transacción incluyendo su historial de estados.
     * Construye el timeline de eventos desde PaymentAuditLog.
     * 
     * @param transactionReference Referencia única de la transacción
     * @return DTO con detalle completo y historial
     */
    TransactionDetailResponse getTransactionWithDetails(String transactionReference);
    
    /**
     * Obtiene el historial cronológico de cambios de estado de una transacción.
     * Filtra logs de auditoría relevantes y los mapea a DTO de timeline.
     * 
     * @param transactionId ID interno de la transacción
     * @return Lista ordenada cronológicamente de cambios de estado
     */
    List<TransactionStatusHistoryDTO> getStatusHistory(Long transactionId);
}
