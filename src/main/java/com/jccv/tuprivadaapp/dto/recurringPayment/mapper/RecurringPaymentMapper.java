package com.jccv.tuprivadaapp.dto.recurringPayment.mapper;

import com.jccv.tuprivadaapp.dto.recurringPayment.RecurringPaymentDto;
import com.jccv.tuprivadaapp.model.recurring_payment.RecurringPayment;
import com.jccv.tuprivadaapp.model.recurring_payment.types.EnumPaymentFrequency;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecurringPaymentMapper {


    private final CondominiumService condominiumService;

    private final ResidentService residentService;

    public RecurringPaymentMapper(CondominiumService condominiumService, ResidentService residentService) {
        this.condominiumService = condominiumService;
        this.residentService = residentService;
    }

    public RecurringPayment toEntity(RecurringPaymentDto recurringPaymentDTO) {
        EnumPaymentFrequency frequency = EnumPaymentFrequency.valueOf(recurringPaymentDTO.getFrequency().toUpperCase());

        RecurringPayment recurringPayment = RecurringPayment.builder()
                .amount(recurringPaymentDTO.getAmount())
                .startDate(recurringPaymentDTO.getStartDate())
                .nextPaymentDate(recurringPaymentDTO.getNextPaymentDate())
                .frequency(frequency)
                .title(recurringPaymentDTO.getTitle())
                .description(recurringPaymentDTO.getDescription())
                .isRecurringPaymentActive(recurringPaymentDTO.isRecurringPaymentActive())
                .condominium(condominiumService.findById(recurringPaymentDTO.getCondominiumId()) )
                .build();

        // Asignamos los residentes al pago recurrente (si vienen en el DTO)
        if (recurringPaymentDTO.getResidentIds() != null) {
            List<Resident> residents = residentService.findAllById(recurringPaymentDTO.getResidentIds());
            recurringPayment.setResidents(residents);
        }

        return recurringPayment;
    }

    public  RecurringPaymentDto toDto(RecurringPayment recurringPayment) {
        return RecurringPaymentDto.builder()
                .id(recurringPayment.getId())
                .amount(recurringPayment.getAmount())
                .startDate(recurringPayment.getStartDate())
                .nextPaymentDate(recurringPayment.getNextPaymentDate())
                .frequency(recurringPayment.getFrequency().toString())
                .isRecurringPaymentActive(recurringPayment.isRecurringPaymentActive())
                .title(recurringPayment.getTitle())
                .description(recurringPayment.getDescription())
                .condominiumId(recurringPayment.getCondominium().getId())
                .residentIds(recurringPayment.getResidents().stream()
                        .map(Resident::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}
