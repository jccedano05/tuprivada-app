package com.jccv.tuprivadaapp.dto.payment.gateway;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para solicitud de creación de cuenta conectada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreationRequest {
    
    @NotNull(message = "El ID del condominio es obligatorio")
    private Long condominiumId;
    
    @NotNull(message = "El nombre del negocio es obligatorio")
    private String businessName;
    
    @NotNull(message = "El tipo de negocio es obligatorio")
    private String businessType;
    
    @NotNull(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    private String phone;
    private String country;
    private String currency;
    
    // Información del representante legal
    private String legalRepresentativeName;
    private String legalRepresentativeEmail;
    private String legalRepresentativePhone;
    
    // Información bancaria
    private String bankAccountNumber;
    private String bankCode;
    private String bankAccountHolderName;
    
    // Información fiscal
    private String taxId; // RFC en México
    private String address;
    private String city;
    private String state;
    private String postalCode;
}
