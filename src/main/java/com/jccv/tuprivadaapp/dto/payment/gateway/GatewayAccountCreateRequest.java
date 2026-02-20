package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para creación de cuenta de pasarela de pago.
 * Diseñado para ser genérico y soportar múltiples proveedores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayAccountCreateRequest {

    @NotNull(message = "El ID del condominio es obligatorio")
    private Long condominiumId;

    @NotNull(message = "El proveedor de pago es obligatorio")
    private PaymentGatewayAccount.PaymentProvider provider;

    @NotBlank(message = "El ID de cuenta del proveedor es obligatorio")
    private String accountId;

    @NotBlank(message = "El nombre de la cuenta es obligatorio")
    private String accountName;

    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email de la cuenta es obligatorio")
    private String accountEmail;

    /**
     * API Key o credencial principal (será encriptada automáticamente).
     */
    @NotBlank(message = "La API key es obligatoria")
    private String apiKey;

    /**
     * Secret key o credencial secundaria si aplica (será encriptada).
     */
    private String secretKey;

    /**
     * Porcentaje de comisión que cobra el proveedor (ej: 3.6).
     */
    private Double commissionPercentage;

    /**
     * Tarifa fija por transacción (ej: 3.00 MXN).
     */
    private Double fixedFee;

    /**
     * Metadata adicional específica del proveedor.
     * Ej: {"merchant_id": "123", "webhook_secret": "whsec_xxx"}
     */
    private Map<String, String> metadata;

    /**
     * Indica si la cuenta debe activarse inmediatamente.
     */
    @Builder.Default
    private Boolean activateImmediately = false;
}
