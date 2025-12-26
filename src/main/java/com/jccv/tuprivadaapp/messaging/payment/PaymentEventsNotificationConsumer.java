package com.jccv.tuprivadaapp.messaging.payment;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.events.payment.PaymentCompletedEvent;
import com.jccv.tuprivadaapp.service.email.EmailService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentEventsNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventsNotificationConsumer.class);

    private final EmailService emailService;
    private final OneSignalPushNotificationService pushNotificationService;

    public PaymentEventsNotificationConsumer(EmailService emailService,
                                             OneSignalPushNotificationService pushNotificationService) {
        this.emailService = emailService;
        this.pushNotificationService = pushNotificationService;
    }

    @KafkaListener(topics = "payments.completed", groupId = "tuprivada-payment-notifications")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("Recibido PaymentCompletedEvent desde Kafka. paymentId={}, userId={}, eventId={}, amount={}",
                    event.getPaymentId(), event.getUserId(), event.getEventId(), event.getAmount());

            pushNotificationService.sendPushToUser(
                    PushNotificationRequest.builder()
                            .title("¡Pago Exitoso!")
                            .message(event.getChargeTitle())
                            .userId(event.getUserId())
                            .build()
            );
            log.info("Push notification enviada exitosamente para paymentId={}, eventId={}",
                    event.getPaymentId(), event.getEventId());

            emailService.sendHtmlEmail(
                    event.getUserEmail(),
                    "¡Pago realizado con éxito!",
                    "payment-success",
                    Map.of(
                            "nombre", event.getUserFirstName(),
                            "monto", String.format("$%.2f", event.getAmount())
                    )
            );
            log.info("Email enviado exitosamente para paymentId={}, eventId={}, email={}",
                    event.getPaymentId(), event.getEventId(), event.getUserEmail());

        } catch (Exception ex) {
            log.error("Error crítico procesando PaymentCompletedEvent. paymentId={}, eventId={}",
                    event.getPaymentId(), event.getEventId(), ex);
            throw ex;
        }
    }
}
