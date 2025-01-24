package com.jccv.tuprivadaapp.service.charge.implementation;

import com.jccv.tuprivadaapp.dto.charge.ChargeDto;
import com.jccv.tuprivadaapp.dto.charge.ChargeSummaryDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentResidentDetailsDto;
import com.jccv.tuprivadaapp.dto.payment.mapper.PaymentMapper;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.charge.ChargeRepository;
import com.jccv.tuprivadaapp.service.charge.ChargeService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChargeServiceImp implements ChargeService {


    private final ChargeRepository chargeRepository;
    private final CondominiumService condominiumService;
    private final PaymentService paymentService;
    private final ResidentService residentService;
    private final PaymentMapper paymentMapper;

    @Autowired
    public ChargeServiceImp(ChargeRepository chargeRepository, CondominiumService condominiumService, @Lazy PaymentService paymentService, ResidentService residentService, PaymentMapper paymentMapper) {
        this.chargeRepository = chargeRepository;
        this.condominiumService = condominiumService;

        this.paymentService = paymentService;
        this.residentService = residentService;
        this.paymentMapper = paymentMapper;
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
        List<Charge> charges = chargeRepository.findByCondominiumIdAndIsActiveTrueOrderByChargeDateDesc(condominiumId);
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
        List<Charge> charges = chargeRepository.findByCondominiumIdAndChargeDateBetweenAndIsActiveTrueOrderByChargeDateDesc(condominiumId, startDate, endDate);
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

    return chargeRepository.save(charge);



}

    @Override
    @Transactional
    public Charge updateCharge(Long chargeId, ChargeDto chargeDto) {
        // Buscar el cargo por ID
        Charge existingCharge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el Cargo con id: " + chargeId));


        double newPaymentCharge = existingCharge.getAmount() - chargeDto.getAmount();



        //UPDATE BALANCE OF RESIDENTS
    if(chargeDto.getAmount() != existingCharge.getAmount()){
          paymentService.getAllPaymentsByChargeId(chargeId).stream().filter(PaymentResidentDetailsDto::isPaid)
                   .forEach(payment -> {
                       residentService.updateBalanceResident(payment.getResidentId(), newPaymentCharge);
                   }); ;
        }


        // Actualizar los campos del cargo
        existingCharge.setTitleTypePayment(chargeDto.getTitleTypePayment());
        existingCharge.setAmount(chargeDto.getAmount());
        existingCharge.setDescription(chargeDto.getDescription());
        existingCharge.setPenaltyType(chargeDto.getPenaltyType());
        existingCharge.setPenaltyValue(chargeDto.getPenaltyValue());
        existingCharge.setChargeDate(chargeDto.getChargeDate());
        existingCharge.setDueDate(chargeDto.getDueDate());

        return chargeRepository.save(existingCharge);
    }


    @Transactional
    @Override
    public void logicalDeleteCharge(Long chargeId) {
        // Buscar el cargo por ID
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el Cargo con id: " + chargeId));

        // Marcar como inactivo (eliminación lógica)
        charge.setActive(false);
        chargeRepository.save(charge);

        // Eliminar lógicamente los pagos asociados al cargo
        paymentService.logicalDeletePaymentsByChargeId(chargeId);
        paymentService.getAllPaymentsByChargeId(chargeId).stream().filter(PaymentResidentDetailsDto::isPaid)
                .forEach(payment -> {
                    residentService.updateBalanceResident(payment.getResidentId(), charge.getAmount());
                }); ;
    }
}

