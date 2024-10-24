package com.jccv.tuprivadaapp.dto.payment.mapper;

import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    // Método para convertir de Payment a PaymentDTO
    public PaymentDto toDTO(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .chargeDate(payment.getChargeDate())
                .dueDate(payment.getDueDate())
                .typePayment(payment.getTypePayment())
                .description(payment.getDescription())
                .isPaid(payment.isPaid())
                .residentId(payment.getResident().getId()) // Asumiendo que quieres el ID del residente
                .build();
    }

    // Método para convertir de PaymentDTO a Payment
    public Payment toEntity(PaymentDto paymentDTO, Resident resident) {
        return Payment.builder()
                .id(paymentDTO.getId())
                .amount(paymentDTO.getAmount())
                .chargeDate(paymentDTO.getChargeDate())
                .dueDate(paymentDTO.getDueDate())
                .typePayment(paymentDTO.getTypePayment())
                .description(paymentDTO.getDescription())
                .isPaid(paymentDTO.isPaid())
                .resident(resident) // Necesitas pasar el residente
                .build();
    }
}
