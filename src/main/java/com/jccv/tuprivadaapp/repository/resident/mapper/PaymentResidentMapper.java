package com.jccv.tuprivadaapp.repository.resident.mapper;

import com.jccv.tuprivadaapp.model.resident.PaymentResident;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.dto.PaymentResidentDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PaymentResidentMapper {


    public PaymentResident paymentDtoToModel(PaymentResidentDto dto) {
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
    public PaymentResident paymentDtoToModel(PaymentResidentDto dto, Resident resident) {
        PaymentResident paymentResident = paymentDtoToModel(dto);
        paymentResident.setResident(resident);

        return paymentResident;
    }

    public  PaymentResidentDto paymentModelToDto(PaymentResident paymentResident) {
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
