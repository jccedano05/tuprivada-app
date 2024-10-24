package com.jccv.tuprivadaapp.repository.resident.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.jccv.tuprivadaapp.model.resident.PaymentResident;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PaymentResidentDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Digits(integer = 5, fraction = 2, message = "El monto debe tener como máximo 5 dígitos enteros y 2 decimales")
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate expirationDate;

    private boolean isComplete;


    @NotBlank(message = "El Id del residente no puede estar vacío")
    private Long residentId;




}
