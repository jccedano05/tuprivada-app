package com.jccv.tuprivadaapp.service.charge.implementation;

import com.jccv.tuprivadaapp.dto.charge.ChargeDto;
import com.jccv.tuprivadaapp.dto.charge.ChargeSummaryDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.repository.charge.ChargeRepository;
import com.jccv.tuprivadaapp.service.charge.ChargeService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChargeServiceImp implements ChargeService {


    private final ChargeRepository chargeRepository;
    private final CondominiumService condominiumService;

    @Autowired
    public ChargeServiceImp(ChargeRepository chargeRepository, CondominiumService condominiumService) {
        this.chargeRepository = chargeRepository;
        this.condominiumService = condominiumService;
    }

//    public Charge createCharge(ChargeDto chargeDto, List<Payment> payments) {
//        Charge charge = Charge.builder()
//                .titleTypePayment(chargeDto.getTitleTypePayment())
//                .chargeDate(chargeDto.getChargeDate())
//                .dueDate(chargeDto.getDueDate())
//                .payments(payments)
//                .isActive(true)
//                .description(chargeDto.getDescription())
//                .penaltyType(chargeDto.getPenaltyType())
//                .penaltyValue(chargeDto.getPenaltyValue())
//                .amount(chargeDto.getAmount())
//                .condominium(condominiumService.findById(chargeDto.getCondominiumId()))
//                .build();
//
//        // Asocia cada pago al cargo
//        payments.forEach(payment -> payment.setCharge(charge));
//
//        return chargeRepository.save(charge);
//    }

    @Override
    public Charge findById(Long chargeId) {
        return chargeRepository.findById(chargeId).orElseThrow(()-> new ResourceNotFoundException("No se encontro el Cargo con el id:" + chargeId));
    }

    @Override
    public List<ChargeSummaryDto> getChargesByCondominiumId(Long condominiumId) {
        List<Charge> charges = chargeRepository.findByCondominiumIdOrderByChargeDateDesc(condominiumId);
        List<ChargeSummaryDto> chargeSummaryDtos = new ArrayList<>();

        for (Charge charge : charges) {
            int totalPayments = charge.getPayments().size(); // O el método adecuado para obtener el total
            int paymentsCompleted = (int) charge.getPayments().stream()
                    .filter(Payment::isPaid) // Suponiendo que tienes un método isPaid en Payment
                    .count();

            ChargeSummaryDto summaryDto = getSummaryDto(charge, totalPayments, paymentsCompleted);

            chargeSummaryDtos.add(summaryDto);
        }

        return chargeSummaryDtos;
    }

    private static ChargeSummaryDto getSummaryDto(Charge charge, int totalPayments, int paymentsCompleted) {
        ChargeSummaryDto summaryDto = new ChargeSummaryDto();
        summaryDto.setId(charge.getId());
        summaryDto.setTitleTypePayment(charge.getTitleTypePayment());
        summaryDto.setChargeDate(charge.getChargeDate());
        summaryDto.setAmount(charge.getAmount());
        summaryDto.setDescription(charge.getDescription());
        summaryDto.setPenaltyType(charge.getPenaltyType());
        summaryDto.setPenaltyValue(charge.getPenaltyValue());
        summaryDto.setDueDate(charge.getDueDate());
        summaryDto.setTotalPayments(totalPayments);
        summaryDto.setPaymentsCompleted(paymentsCompleted);
        summaryDto.setActive(charge.isActive());
        summaryDto.setResidentIds(charge.getPayments().stream().map(payment -> payment.getResident().getId()).toList()); //Obtain list of residents
        return summaryDto;
    }

    @Override
    public List<ChargeSummaryDto> getChargesByCondominiumIdAndDateRange(Long condominiumId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Charge> charges = chargeRepository.findByCondominiumIdAndChargeDateBetweenOrderByChargeDateDesc(condominiumId, startDate, endDate);
        List<ChargeSummaryDto> chargeSummaryDtos = new ArrayList<>();

        for (Charge charge : charges) {
            int totalPayments = charge.getPayments().size(); // O el método adecuado para obtener el total
            int paymentsCompleted = (int) charge.getPayments().stream()
                    .filter(Payment::isPaid) // Suponiendo que tienes un método isPaid en Payment
                    .count();

            ChargeSummaryDto summaryDto = getChargeSummaryDto(charge, totalPayments, paymentsCompleted);


            chargeSummaryDtos.add(summaryDto);
        }

        return chargeSummaryDtos;
    }

    private static ChargeSummaryDto getChargeSummaryDto(Charge charge, int totalPayments, int paymentsCompleted) {
        ChargeSummaryDto summaryDto = getSummaryDto(charge, totalPayments, paymentsCompleted);
        return summaryDto;
    }


    @Transactional
public Charge createCharge(ChargeDto chargeDto) {
    Charge charge = Charge.builder()
            .titleTypePayment(chargeDto.getTitleTypePayment())
            .chargeDate(chargeDto.getChargeDate())
            .dueDate(chargeDto.getDueDate())
            .isActive(true)
            .description(chargeDto.getDescription())
            .penaltyType(chargeDto.getPenaltyType())
            .penaltyValue(chargeDto.getPenaltyValue())
            .amount(chargeDto.getAmount())
            .condominium(condominiumService.findById(chargeDto.getCondominiumId()))
            .build();

    Charge savedCharge = chargeRepository.save(charge);


    return savedCharge;
}

}