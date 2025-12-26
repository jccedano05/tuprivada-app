package com.jccv.tuprivadaapp.messaging.notification;

import com.jccv.tuprivadaapp.dto.events.charge.ChargeCreatedEvent;
import com.jccv.tuprivadaapp.dto.events.payment.PaymentMarkedAsPaidEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationEventProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventProducer.class);
    private static final String CHARGE_CREATED_TOPIC = "charge.created";
    private static final String PAYMENT_MARKED_PAID_TOPIC = "payment.marked.paid";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishChargeCreated(ChargeCreatedEvent event) {
        try {
            log.info("Intentando enviar ChargeCreatedEvent a Kafka. chargeId={}, eventId={}, residentCount={}",
                    event.getChargeId(), event.getEventId(), event.getResidentIds() != null ? event.getResidentIds().size() : 0);

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    CHARGE_CREATED_TOPIC,
                    event.getChargeId().toString(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("ChargeCreatedEvent enviado exitosamente a Kafka. topic={}, partition={}, offset={}, chargeId={}, eventId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.getChargeId(),
                            event.getEventId());
                } else {
                    log.error("Error enviando ChargeCreatedEvent a Kafka. chargeId={}, eventId={}",
                            event.getChargeId(), event.getEventId(), ex);
                }
            });

        } catch (Exception ex) {
            log.error("Excepción crítica al publicar ChargeCreatedEvent. chargeId={}, eventId={}",
                    event.getChargeId(), event.getEventId(), ex);
            throw new RuntimeException("Error crítico publicando evento de cargo creado", ex);
        }
    }

    public void publishPaymentMarkedAsPaid(PaymentMarkedAsPaidEvent event) {
        try {
            log.info("Intentando enviar PaymentMarkedAsPaidEvent a Kafka. paymentId={}, userId={}, eventId={}",
                    event.getPaymentId(), event.getUserId(), event.getEventId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    PAYMENT_MARKED_PAID_TOPIC,
                    event.getPaymentId().toString(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("PaymentMarkedAsPaidEvent enviado exitosamente a Kafka. topic={}, partition={}, offset={}, paymentId={}, eventId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.getPaymentId(),
                            event.getEventId());
                } else {
                    log.error("Error enviando PaymentMarkedAsPaidEvent a Kafka. paymentId={}, eventId={}",
                            event.getPaymentId(), event.getEventId(), ex);
                }
            });

        } catch (Exception ex) {
            log.error("Excepción crítica al publicar PaymentMarkedAsPaidEvent. paymentId={}, eventId={}",
                    event.getPaymentId(), event.getEventId(), ex);
            throw new RuntimeException("Error crítico publicando evento de pago marcado como pagado", ex);
        }
    }
}
