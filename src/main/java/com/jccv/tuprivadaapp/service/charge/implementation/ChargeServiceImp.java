package com.jccv.tuprivadaapp.service.charge.implementation;

import com.jccv.tuprivadaapp.dto.charge.AnnualChargeSummaryDto;
import com.jccv.tuprivadaapp.dto.charge.ChargeDto;
import com.jccv.tuprivadaapp.dto.charge.ChargeSummaryDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentResidentDetailsDto;
import com.jccv.tuprivadaapp.dto.resident.ResidentChargeSummaryDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.charge.ChargeRepository;
import com.jccv.tuprivadaapp.service.charge.ChargeService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChargeServiceImp implements ChargeService {


    private final ChargeRepository chargeRepository;
    private final CondominiumService condominiumService;
    private final PaymentService paymentService;
    private final ResidentService residentService;

    @PersistenceContext
    private EntityManager em;


    @Autowired
    public ChargeServiceImp(ChargeRepository chargeRepository, CondominiumService condominiumService, @Lazy PaymentService paymentService, ResidentService residentService) {
        this.chargeRepository = chargeRepository;
        this.condominiumService = condominiumService;

        this.paymentService = paymentService;
        this.residentService = residentService;
    }


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

    Charge chargeSaved =  chargeRepository.save(charge);




        return chargeSaved;


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

    @Override
    public AnnualChargeSummaryDto getAnnualChargeSummary(Long condominiumId, int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59);

        List<Charge> charges = chargeRepository.findByCondominiumIdAndChargeDateBetweenAndIsActiveTrue(
                condominiumId,
                startDate,
                endDate
        );

        AnnualChargeSummaryDto summary = new AnnualChargeSummaryDto();
        summary.setTotalCharges(charges.size());

        int fullyPaidCount = 0;
        double totalCollected = 0;
        double totalExpected = 0;
        double remainingAmount = 0;
        int totalPendingPayments = 0;
        int totalPaidPayments = 0;


        // Nuevas variables para el conteo
        Map<Long, Integer> residentPendingCounts = new HashMap<>();
        List<Long> allResidentsInYear = new ArrayList<>();

        for (Charge charge : charges) {
            int totalResidents = charge.getPayments().size();
            int paidResidents = 0;

            for (Payment payment : charge.getPayments()) {
                // Registrar residentes con pagos pendientes
                if (!payment.isPaid()) {
                    totalPendingPayments++;
                    residentPendingCounts.merge(payment.getResident().getId(), 1, Integer::sum);
                } else {
                    paidResidents++;
                    totalPaidPayments++; // Contar pagos exitosos
                }

                // Registrar todos los residentes únicos del año
                Long residentId = payment.getResident().getId();
                if (!allResidentsInYear.contains(residentId)) {
                    allResidentsInYear.add(residentId);
                }
            }

            double chargeTotal = charge.getAmount() * totalResidents;
            double chargeCollected = charge.getAmount() * paidResidents;

            totalExpected += chargeTotal;
            totalCollected += chargeCollected;
            remainingAmount += (chargeTotal - chargeCollected);

            if (paidResidents == totalResidents) {
                fullyPaidCount++;
            }
        }

        // Categorizar residentes pendientes
        int count1 = 0, count2 = 0, count3 = 0, count4 = 0, count5Plus = 0;
        for (Map.Entry<Long, Integer> entry : residentPendingCounts.entrySet()) {
            int pendingCount = entry.getValue();
            if (pendingCount >= 5) {
                count5Plus++;
            } else {
                switch (pendingCount) {
                    case 1: count1++; break;
                    case 2: count2++; break;
                    case 3: count3++; break;
                    case 4: count4++; break;
                }
            }
        }

        // Calcular residentes únicos con al menos 1 pendiente
        int uniqueResidentsWithPending = residentPendingCounts.size();

        // Setear valores en el DTO
        summary.setTotalPaidPayments(totalPaidPayments);
        summary.setTotalPendingPayments(totalPendingPayments);
        summary.setFullyPaidCharges(fullyPaidCount);
        summary.setTotalCollected(totalCollected);
        summary.setRemainingAmount(remainingAmount);
        summary.setResidentsWithPending(uniqueResidentsWithPending);

        summary.setResidentsWith1Pending(count1);
        summary.setResidentsWith2Pending(count2);
        summary.setResidentsWith3Pending(count3);
        summary.setResidentsWith4Pending(count4);
        summary.setResidentsWith5PlusPending(count5Plus);

        // Calcular tasa de conversión
        if (totalExpected > 0) {
            double conversionRate = (totalCollected / totalExpected) * 100;
            BigDecimal bd = BigDecimal.valueOf(conversionRate);
            summary.setSuccessConversionRate(bd.setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            summary.setSuccessConversionRate(0);
        }

        return summary;
    }





    @Override
    @Transactional
    public void deleteChargeById(Long chargeId) {
        // Actualizar el balance para los residentes con payments pagados.
        // Se suma el amount del charge a cada residente que tenga algún payment pagado.
        Query queryPaid = em.createNativeQuery(
                "UPDATE residents r " +
                        "SET balance = balance + (SELECT c.amount FROM charges c WHERE c.id = :chargeId) " +
                        "WHERE r.id IN (" +
                        "  SELECT DISTINCT p.resident_id FROM payments p " +
                        "  WHERE p.charge_id = :chargeId AND p.is_paid = true" +
                        ")"
        );
        queryPaid.setParameter("chargeId", chargeId);
        queryPaid.executeUpdate();

        // Actualizar el balance para los residentes con payments no pagados que tienen deposit payments.
        // Se suma la suma de todos los deposit payments asociados a cada payment.
        Query queryNotPaid = em.createNativeQuery(
                "UPDATE residents r " +
                        "SET balance = balance + COALESCE((" +
                        "  SELECT SUM(dp.amount) FROM deposit_payments dp " +
                        "  INNER JOIN payments p ON dp.payment_id = p.id " +
                        "  WHERE p.charge_id = :chargeId AND p.is_paid = false AND p.resident_id = r.id" +
                        "), 0) " +
                        "WHERE r.id IN (" +
                        "  SELECT DISTINCT p.resident_id FROM payments p " +
                        "  WHERE p.charge_id = :chargeId AND p.is_paid = false" +
                        ")"
        );
        queryNotPaid.setParameter("chargeId", chargeId);
        queryNotPaid.executeUpdate();

        // Eliminar todos los payments asociados al charge
        paymentService.deleteAllPaymentsWithChargeId(chargeId);

        // Finalmente, eliminar el charge
        chargeRepository.deleteById(chargeId);
    }
}

