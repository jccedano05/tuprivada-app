package com.jccv.tuprivadaapp.service.recurring_payment.implementation;

import com.jccv.tuprivadaapp.dto.recurringPayment.RecurringPaymentDto;
import com.jccv.tuprivadaapp.dto.recurringPayment.mapper.RecurringPaymentMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.recurring_payment.RecurringPayment;
import com.jccv.tuprivadaapp.model.recurring_payment.types.EnumPaymentFrequency;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.pushNotificacion.OneSignalPushNotificationRepository;
import com.jccv.tuprivadaapp.repository.recurring_payment.RecurringPaymentRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import com.jccv.tuprivadaapp.service.recurring_payment.RecurringPaymentService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecurringPaymentServiceImp  implements RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final PaymentService paymentService;

    private final CondominiumService condominiumService;

    private final ResidentService residentService;

    private final RecurringPaymentMapper recurringPaymentMapper;

    private final OneSignalPushNotificationService oneSignalPushNotificationService;
    private final OneSignalPushNotificationRepository oneSignalPushNotificationRepository;
@Autowired
    public RecurringPaymentServiceImp(RecurringPaymentRepository recurringPaymentRepository, PaymentService paymentService, CondominiumService condominiumService, ResidentService residentService, RecurringPaymentMapper recurringPaymentMapper, OneSignalPushNotificationService oneSignalPushNotificationService, OneSignalPushNotificationRepository oneSignalPushNotificationRepository) {
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.paymentService = paymentService;
    this.condominiumService = condominiumService;
    this.residentService = residentService;
    this.recurringPaymentMapper = recurringPaymentMapper;
    this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    this.oneSignalPushNotificationRepository = oneSignalPushNotificationRepository;
}



    @Override
    @Transactional
    public RecurringPaymentDto create(RecurringPaymentDto paymentDto) {

    //TODO No esta guardando los ids de los residentes en la tabla residents_recurring_payment

        // Guardamos el pago recurrente
        RecurringPayment savedPayment = recurringPaymentRepository.save(recurringPaymentMapper.toEntity(paymentDto));

        return recurringPaymentMapper.toDto(savedPayment);
    }

    @Override
    public RecurringPaymentDto findById(Long id) {
        return recurringPaymentMapper.toDto(recurringPaymentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Recurring Payment not found with Id: " + id)));
    }

    @Override
    public List<RecurringPaymentDto> findAll() {
        return recurringPaymentRepository.findAll().stream().map(recurringPayment -> recurringPaymentMapper.toDto(recurringPayment)).toList();
    }

    @Override
    @Transactional
    public RecurringPaymentDto update(RecurringPaymentDto paymentDto) {
        try {
            // Buscar el pago recurrente por ID
            RecurringPayment existingPayment = recurringPaymentRepository.findById(paymentDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recurring Payment not found with Id: " + paymentDto.getId()));

            // Actualizar los campos necesarios
            existingPayment.setAmount(paymentDto.getAmount());
            existingPayment.setFrequency(EnumPaymentFrequency.valueOf(paymentDto.getFrequency().toUpperCase()));
            existingPayment.setNextPaymentDate(paymentDto.getNextPaymentDate());
            existingPayment.setStartDate(paymentDto.getStartDate());
            existingPayment.setRecurringPaymentActive(paymentDto.isRecurringPaymentActive());

            // Cargar y asignar relaciones (por ejemplo, condominium y residents)
            Condominium condominium = condominiumService.findById(paymentDto.getCondominiumId());
            existingPayment.setCondominium(condominium);

            List<Resident> residents = residentService.findAllById(paymentDto.getResidentIds());
            existingPayment.setResidents(residents);

            // Guardar la entidad actualizada
            RecurringPayment updatedPayment = recurringPaymentRepository.save(existingPayment);

            // Retornar el DTO actualizado
            return recurringPaymentMapper.toDto(updatedPayment);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Recurring Payment", e);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!recurringPaymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurring Payment not found with Id: " + id);
        }
        recurringPaymentRepository.deleteById(id);
    }


    // Revisa todos los días a las 4:00 AM si hay pagos que deben procesarse
    @Scheduled(cron = "0 0 10 * * *", zone = "America/Mexico_City")
    @Transactional
    public void processPayments() {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Starting processPayments...");


        try {
            List<RecurringPayment> paymentsDueToday = recurringPaymentRepository.findByNextPaymentDate();


            for (RecurringPayment recurringPayment : paymentsDueToday) {

                try {

                    List<Payment> payments = new ArrayList<>();
                    recurringPayment.getResidents().forEach(paymentResident -> {

                        if(paymentResident.isActiveResident()){
                            Payment payment = Payment.builder()
//                                    .amount(recurringPayment.getAmount())
//                                    .typePayment(recurringPayment.getTitle())
//                                    .description(recurringPayment.getDescription())
//                                    .chargeDate(LocalDateTime.now())
//                                    .dueDate(LocalDateTime.now().plusDays(recurringPayment.getDaysToDueDate()))
                                    .resident(paymentResident)
                                    .isPaid(false)
                                    .build();
                            payments.add(payment);
                        }

                        oneSignalPushNotificationRepository.findByUserId(paymentResident.getUser().getId()).forEach(oneSignalPushNotification -> {
                            oneSignalPushNotificationService.sendNotification(oneSignalPushNotification.getSubscriptionId(), "Cobro Generado", recurringPayment.getDescription());
                        });

                    });

                    paymentService.createAll(payments);


                    recurringPayment.scheduleNextPayment();  // Programa el próximo pago
                    recurringPaymentRepository.save(recurringPayment);  // Guarda el próximo pago

                } catch (Exception e) {
                    logger.error("Error processing recurring payment ID {}: {}", recurringPayment.getId(), e.getMessage(), e);
                    // Puedes manejar el error aquí, por ejemplo, enviando una alerta o rollback manual si es necesario
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            logger.error("Error during processPayments: {}", e.getMessage(), e);
        }
        logger.info("Finished processPayments.");
    }
}
