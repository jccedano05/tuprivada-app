package com.jccv.tuprivadaapp.service.payment.gateway;

/**
 * Servicio para generación de comprobantes de pago en formato PDF.
 * Crea documentos profesionales con toda la información de la transacción.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
public interface PaymentReceiptService {
    
    /**
     * Genera un comprobante de pago en formato PDF.
     * Solo disponible para transacciones con estado SUCCEEDED.
     * 
     * El PDF incluye:
     * - Logo del condominio o genérico de TuPrivada
     * - Información completa de la transacción
     * - Código QR con la referencia de transacción
     * - Detalles del residente
     * - Información del cargo
     * - Desglose de comisiones
     * - Fecha y hora de acreditación
     * 
     * @param transactionReference Referencia única de la transacción
     * @return Array de bytes del PDF generado
     * @throws com.jccv.tuprivadaapp.exception.ResourceNotFoundException si la transacción no existe
     * @throws com.jccv.tuprivadaapp.exception.BadRequestException si la transacción no está en estado SUCCEEDED
     */
    byte[] generateReceipt(String transactionReference);
}
