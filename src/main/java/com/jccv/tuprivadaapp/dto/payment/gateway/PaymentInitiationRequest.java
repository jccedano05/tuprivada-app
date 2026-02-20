package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para iniciar un pago desde un Payment existente usando una pasarela.
 * Incluye validación de email obligatorio para notificaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiationRequest {
    
    @NotNull(message = "El ID del pago es obligatorio")
    private Long paymentId;
    
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentTransaction.PaymentMethod paymentMethod;
    
    @NotBlank(message = "El email es obligatorio para enviar notificaciones")
    @Email(message = "El email debe ser válido")
    private String email;
    
    private PaymentGatewayAccount.PaymentProvider provider;
    
    private String cardToken;
    
    private Integer expiresInDays;
    
    private String deviceFingerprint;
    
    private String ipAddress;
}
