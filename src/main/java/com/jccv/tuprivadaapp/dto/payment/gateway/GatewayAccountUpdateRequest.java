package com.jccv.tuprivadaapp.dto.payment.gateway;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para actualización de cuenta de pasarela de pago.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayAccountUpdateRequest {

    private String accountName;

    @Email(message = "El email debe ser válido")
    private String accountEmail;

    /**
     * Nueva API Key (será re-encriptada si se proporciona).
     */
    private String apiKey;

    /**
     * Nueva Secret Key (será re-encriptada si se proporciona).
     */
    private String secretKey;

    private Double commissionPercentage;

    private Double fixedFee;

    /**
     * Metadata adicional a actualizar (se mergea con la existente).
     */
    private Map<String, String> metadata;
}
