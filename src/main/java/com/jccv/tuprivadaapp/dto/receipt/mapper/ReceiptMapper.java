package com.jccv.tuprivadaapp.dto.receipt.mapper;


import com.jccv.tuprivadaapp.dto.receipt.ReceiptDto;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.receipt.Receipt;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReceiptMapper {

    private final CondominiumService condominiumService;

    @Autowired
    public ReceiptMapper(CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public ReceiptDto toDTO(Receipt receipt) {
        if (receipt == null) return null;

        return ReceiptDto.builder()
                .id(receipt.getId())
                .receiptName(receipt.getReceiptName())
                .operationCode(receipt.getOperationCode())
                .createdAt(receipt.getCreatedAt())
                .paymentId(receipt.getPayment() != null ? receipt.getPayment().getId() : null)
                .depositPaymentId(receipt.getDepositPayment() != null ? receipt.getDepositPayment().getId() : null)
                .condominiumId(receipt.getCondominium().getId())
                .title(receipt.getTitle())
                .description(receipt.getDescription())
                .residentAddress(receipt.getResidentAddress())
                .residentName(receipt.getResidentName())
                .amount(receipt.getAmount())
                .datePaid(receipt.getDatePaid())
                .build();
    }

    public Receipt toEntity(ReceiptDto dto) {
        if (dto == null) return null;

        Condominium condominium = condominiumService.findById(dto.getCondominiumId());

        return Receipt.builder()
                .id(dto.getId())
                .receiptName(dto.getReceiptName())
                .operationCode(dto.getOperationCode())
                .createdAt(dto.getCreatedAt())
                .condominium(condominium)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .residentAddress(dto.getResidentAddress())
                .residentName(dto.getResidentName())
                .amount(dto.getAmount())
                .datePaid(dto.getDatePaid())
                .build();
    }
}
