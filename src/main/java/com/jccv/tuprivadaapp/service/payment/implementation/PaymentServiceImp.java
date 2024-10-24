package com.jccv.tuprivadaapp.service.payment.implementation;

import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentSummaryDto;
import com.jccv.tuprivadaapp.dto.payment.mapper.PaymentMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.PenaltyTypeEnum;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;
    private final ResidentService residentService;

    private final AccountBankService accountBankService;

    @Autowired
    public PaymentServiceImp(PaymentRepository paymentRepository, PaymentMapper paymentMapper, ResidentService residentService, AccountBankService accountBankService) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.residentService = residentService;
        this.accountBankService = accountBankService;
    }

    @Override
    public PaymentDto create(PaymentDto paymentDto) {
            Resident resident = residentService.getResidentById(paymentDto.getResidentId()).orElseThrow(()->new ResourceNotFoundException("Resident not found with id: " + paymentDto.getResidentId()));
             Payment payment = paymentRepository.save(paymentMapper.toEntity(paymentDto,resident));
            return paymentMapper.toDTO(payment);
    }

    @Override
    public List<Payment> createAll(List<Payment> payments) {
        return paymentRepository.saveAll(payments);
    }

    @Override
    public PaymentDto findById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Payiment not found with Id: " + id));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream().map(payment -> paymentMapper.toDTO(payment)).toList();
    }

    @Override
    public PaymentDto update(PaymentDto paymentDto) {
        Payment existingPayment = paymentRepository.findById(paymentDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with Id: " + paymentDto.getId()));

        Resident resident = residentService.getResidentById(paymentDto.getResidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + paymentDto.getResidentId()));

        Payment updatedPayment = paymentMapper.toEntity(paymentDto, resident);

        updatedPayment.setId(existingPayment.getId());

        updatedPayment = paymentRepository.save(updatedPayment);

        return paymentMapper.toDTO(updatedPayment);
    }

    @Override
    public void deleteById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        // Realizar el borrado lógico, marcando el pago como eliminado
        payment.setDeleted(true);
        paymentRepository.save(payment);
    }




//    public List<Payment> getUnpaidPaymentsForResident(Long residentId) {
//        // Buscar al residente por su ID
//        Resident resident = residentService.getResidentById(residentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with ID: " + residentId));
//
//        // Obtener pagos no pagados y no eliminados del residente
//        return paymentRepository.findByResidentAndIsPaidFalseAndIsDeletedFalse(resident);
//    }

    public Page<Payment> getUnpaidPaymentsForResident(Long residentId, int page, int size) {
        // Crear un Pageable para la paginación
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("chargeDate")));

        // Llamar al repositorio para obtener los pagos no pagados y no eliminados, ordenados por fecha de carga
        return paymentRepository.findByResidentIdAndIsPaidFalseAndIsDeletedFalse(residentId, pageable);
    }

    public Page<Payment> getPaidPaymentsForResident(Long residentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("chargeDate")));
        return paymentRepository.findByResidentIdAndIsPaidTrueAndIsDeletedFalseOrderByChargeDateDesc(residentId, pageable);
    }


    public Page<Payment> getPaymentsInRangeForResident(Long residentId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("chargeDate")));
        return paymentRepository.findByResidentIdAndChargeDateBetweenAndIsDeletedFalseOrderByChargeDateDesc(residentId, startDate, endDate, pageable);
    }

    // Método para obtener el total de deuda de un residente
    public Double getTotalDebtForResident(Long residentId) {
        return paymentRepository.getTotalDebtForResident(residentId);
    }

    // Método para obtener la próxima fecha de vencimiento de un residente
    public LocalDateTime getRelevantDueDateForResident(Long residentId) {

        List<LocalDateTime> dateTimes = paymentRepository.getRelevantDueDateForResident(residentId);
        if (!dateTimes.isEmpty()) {
            LocalDateTime firstDate = dateTimes.get(0);
            LocalDateTime currentDate = LocalDateTime.now(); // Obtener la fecha actual
            if (firstDate.isAfter(currentDate) || firstDate.isEqual(currentDate)) {
                return firstDate; // Retorna el primer valor si es mayor o igual a la fecha actual
            } else {
                return dateTimes.get(dateTimes.size() - 1); // Retorna el último valor si el primero es menor
            }
        }

        return null; // Opcional: manejar el caso si la lista está vacía
    }


    // Método que combina ambos en un solo DTO
    public PaymentSummaryDto getPaymentSummaryForResident(Long residentId) {
        return  PaymentSummaryDto.builder()
                .nextDueDate(getRelevantDueDateForResident(residentId))
                .totalDebt(getTotalDebtForResident(residentId))
                .accountBankDto(accountBankService.findAccountBankByResident(residentId))
                .build();
    }



    @Scheduled(cron = "0 0 4 * * *", zone = "America/Mexico_City")
    @Transactional
    public void processOverduePayments() {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Starting processOverduePayments...");

        try {
            // Buscar todos los pagos vencidos que no se han pagado y aún no tienen recargo aplicado
            List<Payment> overduePayments = paymentRepository.findOverduePayments();

            for (Payment overduePayment : overduePayments) {
                try {
                    logger.info("Processing overdue payment ID: {} for resident ID: {}",
                            overduePayment.getId(), overduePayment.getResident().getId());

                    // Lógica para aplicar el recargo según el tipo (fijo o porcentaje)
                    double penalty = 0;
                    if (overduePayment.getPenaltyType() == PenaltyTypeEnum.PERCENTAGE) {
                        penalty = overduePayment.getAmount() * (overduePayment.getPenaltyValue() / 100);
                    } else if (overduePayment.getPenaltyType() == PenaltyTypeEnum.FIXED_AMOUNT) {
                        penalty = overduePayment.getPenaltyValue();
                    }

                    // Actualizar el monto del pago sumando el recargo
                    overduePayment.setAmount(overduePayment.getAmount() + penalty);

                    // Actualizar la descripción para indicar que el pago está vencido
                    overduePayment.setDescription(overduePayment.getDescription() + " (Vencido)");

                    // Marcar el recargo como aplicado
                    overduePayment.setPenaltyApplied(true);

                    // Guardar los cambios en la base de datos
                    paymentRepository.save(overduePayment);

                } catch (Exception e) {
                    logger.error("Error processing overdue payment ID {}: {}", overduePayment.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error during processOverduePayments: {}", e.getMessage(), e);
        }

        logger.info("Finished processOverduePayments.");
    }
}