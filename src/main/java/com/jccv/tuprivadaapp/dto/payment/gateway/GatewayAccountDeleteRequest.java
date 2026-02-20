package com.jccv.tuprivadaapp.dto.payment.gateway;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de eliminación de cuenta de pasarela.
 * Permite enviar la razón en el body en lugar de query param.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayAccountDeleteRequest {
    
    @NotBlank(message = "La razón de eliminación es obligatoria")
    private String reason;
}
