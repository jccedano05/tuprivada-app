package com.jccv.tuprivadaapp.messaging.notification;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.events.charge.ChargeCreatedEvent;
import com.jccv.tuprivadaapp.dto.events.payment.PaymentMarkedAsPaidEvent;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventsConsumer.class);

    private final OneSignalPushNotificationService pushNotificationService;

    public NotificationEventsConsumer(OneSignalPushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @KafkaListener(topics = "charge.created", groupId = "tuprivada-notifications", containerFactory = "kafkaListenerContainerFactory")
    public void handleChargeCreated(ChargeCreatedEvent event) {
        try {
            log.info("Recibido ChargeCreatedEvent desde Kafka. chargeId={}, eventId={}, residentCount={}",
                    event.getChargeId(), event.getEventId(), event.getResidentIds() != null ? event.getResidentIds().size() : 0);

            if (event.getResidentIds() != null && !event.getResidentIds().isEmpty()) {
                pushNotificationService.sendPushToResidentsList(
                        event.getResidentIds(),
                        PushNotificationRequest.builder()
                                .title("Nuevo cargo")
                                .message("El cargo '" + event.getChargeTitle() + "' ya está disponible para pagar")
                                .build()
                );
                log.info("Push notifications enviadas exitosamente para ChargeCreatedEvent. chargeId={}, eventId={}, residentCount={}",
                        event.getChargeId(), event.getEventId(), event.getResidentIds().size());
            } else {
                log.warn("ChargeCreatedEvent sin residentIds. chargeId={}, eventId={}", event.getChargeId(), event.getEventId());
            }

        } catch (Exception ex) {
            log.error("Error crítico procesando ChargeCreatedEvent. chargeId={}, eventId={}",
                    event.getChargeId(), event.getEventId(), ex);
            throw ex;
        }
    }

    @KafkaListener(topics = "payment.marked.paid", groupId = "tuprivada-notifications", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentMarkedAsPaid(PaymentMarkedAsPaidEvent event) {
        try {
            log.info("Recibido PaymentMarkedAsPaidEvent desde Kafka. paymentId={}, userId={}, eventId={}",
                    event.getPaymentId(), event.getUserId(), event.getEventId());

            pushNotificationService.sendPushToUser(
                    PushNotificationRequest.builder()
                            .title("¡Pago Exitoso!")
                            .message(event.getChargeTitle())
                            .userId(event.getUserId())
                            .build()
            );
            log.info("Push notification enviada exitosamente para PaymentMarkedAsPaidEvent. paymentId={}, userId={}, eventId={}",
                    event.getPaymentId(), event.getUserId(), event.getEventId());

        } catch (Exception ex) {
            log.error("Error crítico procesando PaymentMarkedAsPaidEvent. paymentId={}, eventId={}",
                    event.getPaymentId(), event.getEventId(), ex);
            throw ex;
        }
    }
}
