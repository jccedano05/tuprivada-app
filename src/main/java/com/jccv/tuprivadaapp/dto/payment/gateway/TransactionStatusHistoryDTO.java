package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para representar un cambio de estado en el historial de una transacción.
 * Utilizado para construir timeline visual en el frontend.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatusHistoryDTO {
    
    /**
     * Estado de la transacción en este punto del historial
     */
    private PaymentTransaction.TransactionStatus status;
    
    /**
     * Timestamp cuando ocurrió este cambio de estado
     */
    private LocalDateTime timestamp;
    
    /**
     * Mensaje descriptivo del cambio de estado
     */
    private String message;
    
    /**
     * Indica quién/qué provocó el cambio de estado
     * Valores posibles: "Sistema", "Webhook Conekta", "Administrador", "Usuario"
     */
    private String performedBy;
    
    /**
     * Información adicional sobre el evento
     */
    private String additionalInfo;
    
    /**
     * Indica si este es un estado final (SUCCEEDED, FAILED, CANCELLED, etc.)
     */
    private Boolean isFinalState;
    
    /**
     * Calcula si el estado es final basado en el enum
     */
    public Boolean getIsFinalState() {
        if (status == null) {
            return false;
        }
        
        return status == PaymentTransaction.TransactionStatus.SUCCEEDED ||
               status == PaymentTransaction.TransactionStatus.CAPTURED ||
               status == PaymentTransaction.TransactionStatus.FAILED ||
               status == PaymentTransaction.TransactionStatus.CANCELLED ||
               status == PaymentTransaction.TransactionStatus.REFUNDED ||
               status == PaymentTransaction.TransactionStatus.EXPIRED;
    }
}
