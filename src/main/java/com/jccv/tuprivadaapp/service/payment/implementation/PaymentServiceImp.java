package com.jccv.tuprivadaapp.service.payment.implementation;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.payment.*;
import com.jccv.tuprivadaapp.dto.payment.mapper.PaymentMapper;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.service.charge.ChargeService;
import com.jccv.tuprivadaapp.service.payment.DepositPaymentService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;

import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;
    private final ResidentService residentService;

    private final ChargeService chargeService;
    private final DepositPaymentService depositPaymentService;

    private final PollingNotificationService pollingNotificationService;

    private final OneSignalPushNotificationService oneSignalPushNotificationService;


    @Autowired
    public PaymentServiceImp(OneSignalPushNotificationService oneSignalPushNotificationService , PaymentRepository paymentRepository, PaymentMapper paymentMapper, ResidentService residentService, @Lazy ChargeService chargeService, DepositPaymentService depositPaymentService, PollingNotificationService pollingNotificationService) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.residentService = residentService;
        this.chargeService = chargeService;
        this.depositPaymentService = depositPaymentService;
        this.pollingNotificationService = pollingNotificationService;
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }

    @Override
    public PaymentDto create(PaymentDto paymentDto) {
            Resident resident = residentService.getResidentById(paymentDto.getResidentId()).orElseThrow(()->new ResourceNotFoundException("Resident not found with id: " + paymentDto.getResidentId()));

             Payment payment = paymentRepository.save(paymentMapper.toEntity(paymentDto,resident));

            return paymentMapper.toDTO(payment);
    }


    @Override
    public PaymentDto findById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Payiment not found with Id: " + id));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream().map(paymentMapper::toDTO).toList();
    }

    @Override
    @Transactional
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


    @Transactional
    public void deletePaymentByResidentIdAndChargeId(Long residentId, Long chargeId) {
        Payment payment = paymentRepository.findByResidentIdAndChargeId(residentId, chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for residentId: "
                        + residentId + " and chargeId: " + chargeId));



        if(payment.isPaid()){
            residentService.updateBalanceResident(residentId, payment.getCharge().getAmount());
            depositPaymentService.deleteAllDepositsByPaymentId(payment.getId());
        }else{
            depositPaymentService.deleteAllDepositsWithBalanceUpdateByPaymentId(payment.getId(), payment.getResident());
        }
        paymentRepository.delete(payment);

    }



    @Override
    @Transactional
    public void updateIsPaidStatus(Long chargeId, Long residentId, Boolean isPaid) {
        Resident resident = residentService.getResidentById(residentId).orElseThrow(()-> new ResourceNotFoundException("No se encontro al residente con el ID: " + residentId));



        Payment payment = paymentRepository.findByResidentIdAndChargeId(residentId, chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for residentId: "
                        + residentId + " and chargeId: " + chargeId));
        payment.setPaid(isPaid);

        double totalDepositsPayment = depositPaymentService.getTotalDepositsAmountByPaymentId(payment.getId());

        double balanceAfterPaid = resident.getBalance() - payment.getCharge().getAmount() + totalDepositsPayment;
        if(isPaid && balanceAfterPaid < 0){
            throw new BadRequestException("Saldo insuficiente para hacer el pago");
        }
        if(isPaid){
            payment.setDatePaid(LocalDateTime.now());
        }else{
            payment.setDatePaid(null);
        }
        paymentRepository.save(payment);


        double newBalance = 0;
        if(isPaid){
             newBalance -= payment.getCharge().getAmount() - totalDepositsPayment;
           Charge charge = chargeService.findById(chargeId);
           pollingNotificationService.createNotification(PollingNotificationDto.builder()
                   .title("Pago: " + charge.getTitleTypePayment())
                   .message(charge.getDescription())
                   .userId(resident.getUser().getId())
                   .read(false)
                   .build());

            oneSignalPushNotificationService.sendPushToUser(PushNotificationRequest.builder()
                    .title("Pago Exitoso.!")
                    .message(charge.getTitleTypePayment())
                    .userId(resident.getUser().getId())
                    .build());
       }else{

             newBalance += payment.getCharge().getAmount() - totalDepositsPayment;
       }
        residentService.updateBalanceResident(resident, newBalance);
    }

    @Override
    public List<PaymentResidentDetailsDto> getAllPaymentsByChargeId(Long chargeId) {
        return paymentRepository.findAllByChargeId(chargeId);
    }

    @Override
    @Transactional
    public void logicalDeletePaymentsByChargeId(Long chargeId) {
        paymentRepository.markPaymentsAsDeletedByChargeId(chargeId);
    }

    @Override
    public void deleteAllPaymentsWithChargeId(Long chargeId) {
        paymentRepository.deleteAllPaymentsWithChargeId(chargeId);
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

    public Page<PaymentDetailsDto> getUnpaidPaymentsForResident(Long residentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findByResidentIdAndIsPaidFalseAndIsDeletedFalse(residentId, pageable);
    }

    public Page<PaymentDetailsDto> getPaidPaymentsForResident(Long residentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findByResidentIdAndIsPaidTrueAndIsDeletedFalse(residentId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionDto> getResidentTransactions(Long residentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Obtener la página de pagos
        Page<PaymentDetailsDto> paymentPage = paymentRepository.findByResidentIdAndIsPaidTrueAndIsDeletedFalse(residentId, pageable);

        // Definir el rango de fechas para buscar los depósitos:
        // Usamos la fecha máxima de chargeDate de los pagos y la fecha actual
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = paymentPage.getContent().stream()
                .map(PaymentDetailsDto::getChargeDate)
                .max(LocalDateTime::compareTo)
                .orElse(endDate);

        // Obtener los depósitos dentro del rango
        List<DepositPaymentDto> depositList = depositPaymentService.getDepositsByResidentAndDateRange(residentId, startDate, endDate);

        // Mapear los pagos a TransactionDto
        List<TransactionDto> transactionsFromPayments = paymentPage.getContent().stream()
                .map(payment -> TransactionDto.builder()
                        .transactionId(payment.getPaymentId())
                        .transactionType("PAYMENT")
                        .title(payment.getTitleTypePayment())
                        .date(payment.getChargeDate())
                        .amount(payment.getAmount())
                        .description(payment.getDescription())
                        .build())
                .toList();

        // Mapear los depósitos a TransactionDto
        List<TransactionDto> transactionsFromDeposits = depositList.stream()
                .map(deposit -> TransactionDto.builder()
                        .transactionId(deposit.getId())
                        .transactionType("DEPOSIT")
                        .date(deposit.getDepositDate())
                        .title(deposit.getTitle())
                        .amount(deposit.getAmount())
                        .description(deposit.getDescription())
                        .build())
                .toList();

        // Combinar ambas listas y ordenarlas de la fecha más reciente a la más antigua
        List<TransactionDto> allTransactions = Stream.concat(transactionsFromPayments.stream(), transactionsFromDeposits.stream())
                .sorted(Comparator.comparing(TransactionDto::getDate).reversed())
                .collect(Collectors.toList());

        // Retornar un Page a partir de la lista combinada (puedes ajustar la paginación según lo necesites)
        return new PageImpl<>(allTransactions, pageable, allTransactions.size());
    }


    public Page<PaymentDetailsDto> getPaymentsInRangeForResident(Long residentId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findByResidentIdAndChargeDateBetweenAndIsDeletedFalse(
                residentId, startDate, endDate, pageable);
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
                .build();
    }

//    @Transactional
//    public List<Payment> applyChargeToResidents(List<Resident> residents, ChargeDto chargeDto) {
//        List<Payment> payments = new ArrayList<>();
//        for (Resident resident : residents) {
//            Payment payment = Payment.builder()
//                    .resident(resident)
//                    .amount(chargeDto.getAmount())
//                    .chargeDate(chargeDto.getChargeDate())
//                    .dueDate(chargeDto.getDueDate())  // Por ejemplo, un mes de vencimiento
//                    .description(chargeDto.getDescription())
//                    .isPaid(false)
//                    .isDeleted(false)
//                    .typePayment(chargeDto.getTypePayment())
//                    .penaltyType(chargeDto.getPenaltyType())
//                    .penaltyValue(chargeDto.getPenaltyValue())
//                    .build();
//
//            payments.add(paymentRepository.save(payment));
//        }
//
//        return payments;
//    }

    @Transactional
    public List<Payment> applyChargeToResidents(List<Resident> residents, Charge charge) {
        // Genera los pagos para cada residente
        List<Payment> payments = residents.stream().map(resident -> {
            Payment payment = Payment.builder()
//                    .amount(chargeDto.getAmount())
//                    .chargeDate(LocalDateTime.now())
//                    .dueDate(chargeDto.getDueDate())
//                    .typePayment(chargeDto.getTitleTypePayment())
//                    .description(chargeDto.getDescription())
                    .charge(charge)
                    .resident(resident)
                    .isPaid(false)
                    .build();

            Payment paymentSaved = paymentRepository.save(payment);

//            pollingNotificationService.createNotification(PollingNotificationDto.builder()
//                    .title("Nuevo Cargo aplicado")
//                    .message("El cargo '" + payment.getCharge().getDescription() + "' ya esta disponible para pagar")
//                    .userId(resident.getUser().getId())
//                    .read(false)
//                    .build());


            return paymentSaved;
        }).collect(Collectors.toList());


        return payments;
    }

//
//    @Scheduled(cron = "0 0 4 * * *", zone = "America/Mexico_City")
//    @Transactional
//    public void processOverduePayments() {
//        Logger logger = LoggerFactory.getLogger(getClass());
//        logger.info("Starting processOverduePayments...");
//
//        try {
//            // Buscar todos los pagos vencidos que no se han pagado y aún no tienen recargo aplicado
//            List<Payment> overduePayments = paymentRepository.findOverduePayments();
//
//            for (Payment overduePayment : overduePayments) {
//                try {
//                    logger.info("Processing overdue payment ID: {} for resident ID: {}",
//                            overduePayment.getId(), overduePayment.getResident().getId());
//
//                    // Lógica para aplicar el recargo según el tipo (fijo o porcentaje)
//                    double penalty = 0;
//                    if (overduePayment.getPenaltyType() == PenaltyTypeEnum.PERCENTAGE) {
//                        penalty = overduePayment.getAmount() * (overduePayment.getPenaltyValue() / 100);
//                    } else if (overduePayment.getPenaltyType() == PenaltyTypeEnum.FIXED_AMOUNT) {
//                        penalty = overduePayment.getPenaltyValue();
//                    }
//
//                    // Actualizar el monto del pago sumando el recargo
//                    overduePayment.setAmount(overduePayment.getAmount() + penalty);
//
//                    // Actualizar la descripción para indicar que el pago está vencido
//                    overduePayment.setDescription(overduePayment.getDescription() + " (Vencido)");
//
//                    // Marcar el recargo como aplicado
//                    overduePayment.setPenaltyApplied(true);
//
//                    // Guardar los cambios en la base de datos
//                    paymentRepository.save(overduePayment);
//
//                } catch (Exception e) {
//                    logger.error("Error processing overdue payment ID {}: {}", overduePayment.getId(), e.getMessage(), e);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error during processOverduePayments: {}", e.getMessage(), e);
//        }
//
//        logger.info("Finished processOverduePayments.");
//    }
}