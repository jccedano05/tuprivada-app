package com.jccv.tuprivadaapp.messaging.payment;

import com.jccv.tuprivadaapp.dto.events.payment.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payments.completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("Intentando enviar PaymentCompletedEvent a Kafka. paymentId={}, userId={}, eventId={}",
                    event.getPaymentId(), event.getUserId(), event.getEventId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    TOPIC,
                    event.getPaymentId().toString(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("PaymentCompletedEvent enviado exitosamente a Kafka. topic={}, partition={}, offset={}, paymentId={}, eventId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.getPaymentId(),
                            event.getEventId());
                } else {
                    log.error("Error enviando PaymentCompletedEvent a Kafka. paymentId={}, eventId={}",
                            event.getPaymentId(), event.getEventId(), ex);
                }
            });

        } catch (Exception ex) {
            log.error("Excepción crítica al publicar PaymentCompletedEvent. paymentId={}, eventId={}",
                    event.getPaymentId(), event.getEventId(), ex);
            throw new RuntimeException("Error crítico publicando evento de pago completado", ex);
        }
    }
}
