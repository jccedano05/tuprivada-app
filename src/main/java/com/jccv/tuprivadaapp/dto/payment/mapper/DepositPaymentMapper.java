package com.jccv.tuprivadaapp.dto.payment.mapper;

import com.jccv.tuprivadaapp.dto.payment.DepositPaymentDto;
import com.jccv.tuprivadaapp.model.payment.DepositPayment;
import org.springframework.stereotype.Component;

@Component
public class DepositPaymentMapper {

    public DepositPaymentDto toDTO(DepositPayment depositPayment) {
        return DepositPaymentDto.builder()
                .id(depositPayment.getId())
                .paymentId(depositPayment.getPayment().getId())
                .amount(depositPayment.getAmount())
                .depositDate(depositPayment.getDepositDate())
                .title(depositPayment.getTitle())
                .description(depositPayment.getDescription())
                .build();
    }

    public DepositPayment toEntity(DepositPaymentDto depositPaymentDTO) {
        return DepositPayment.builder()
                .id(depositPaymentDTO.getId())
                .amount(depositPaymentDTO.getAmount())
                .depositDate(depositPaymentDTO.getDepositDate())
                .title(depositPaymentDTO.getTitle())
                .description(depositPaymentDTO.getDescription())
                .build();
    }
}
