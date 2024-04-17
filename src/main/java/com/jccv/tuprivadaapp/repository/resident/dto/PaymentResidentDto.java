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


    public static PaymentResident paymentDtoToModel(PaymentResidentDto dto) {
        LocalDate date = LocalDate.now();
        LocalDate expirationDate = LocalDate.now().plusDays(15);

        if(dto.getExpirationDate() != null){
            expirationDate = dto.getExpirationDate();
        }
        if(dto.getDate() != null){
            date = dto.getDate();
        }
        PaymentResident paymentResident = PaymentResident.builder()
                .id(dto.getId())
                .amount(dto.getAmount())
                .date(date)
                .expirationDate(expirationDate)
                .isComplete(dto.isComplete())
                .build();

        return paymentResident;
    }
    public static PaymentResident paymentDtoToModel(PaymentResidentDto dto, Resident resident) {
        PaymentResident paymentResident = paymentDtoToModel(dto);
        paymentResident.setResident(resident);

        return paymentResident;
    }

    public static PaymentResidentDto paymentModelToDto(PaymentResident paymentResident) {
        Long residentId = null;
        if(paymentResident.getResident() != null){
            residentId = paymentResident.getResident().getId();
        }
        PaymentResidentDto dto = PaymentResidentDto.builder()
                .id(paymentResident.getId())
                .amount(paymentResident.getAmount())
                .date(paymentResident.getDate())
                .expirationDate(paymentResident.getExpirationDate())
                .isComplete(paymentResident.isComplete())
                .residentId(residentId)
                .build();

        return dto;
    }


}
