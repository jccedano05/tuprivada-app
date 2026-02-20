package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuesta completa del detalle de una transacción.
 * Incluye la información de la transacción y su historial de cambios de estado.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {
    
    /**
     * Información completa de la transacción
     */
    private TransactionListItemDTO transaction;
    
    /**
     * Email del residente dueño del pago
     */
    private String residentEmail;
    
    /**
     * Nombre del residente dueño del pago
     */
    private String residentName;
    
    /**
     * Referencia bancaria personal del residente
     */
    private String bankPersonalReference;
    
    /**
     * Historial cronológico de cambios de estado
     * Ordenado de más antiguo a más reciente
     */
    private List<TransactionStatusHistoryDTO> statusHistory;
    
    /**
     * Información del cargo asociado al pago
     */
    private ChargeInfoDTO chargeInfo;
    
    /**
     * Información de la cuenta de pasarela utilizada
     */
    private GatewayAccountInfoDTO gatewayAccountInfo;
    
    /**
     * DTO interno para información básica del cargo
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeInfoDTO {
        private Long chargeId;
        private String description;
        private String titleTypePayment;
        private String dueDate;
    }
    
    /**
     * DTO interno para información básica de la cuenta de pasarela
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayAccountInfoDTO {
        private Long accountId;
        private String provider;
        private String accountName;
    }
}
