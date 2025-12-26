package com.jccv.tuprivadaapp.messaging.notification;

import com.jccv.tuprivadaapp.dto.events.charge.ChargeCreatedEvent;
import com.jccv.tuprivadaapp.dto.events.payment.PaymentMarkedAsPaidEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationEventProducer notificationEventProducer;

    private ChargeCreatedEvent chargeCreatedEvent;
    private PaymentMarkedAsPaidEvent paymentMarkedAsPaidEvent;

    @BeforeEach
    void setUp() {
        chargeCreatedEvent = new ChargeCreatedEvent(
                1L,
                "Mantenimiento",
                "Pago de mantenimiento mensual",
                500.0,
                Arrays.asList(1L, 2L, 3L)
        );

        paymentMarkedAsPaidEvent = new PaymentMarkedAsPaidEvent(
                100L,
                200L,
                300L,
                "test@example.com",
                "Juan",
                "Mantenimiento",
                "Pago mensual",
                500.0
        );
    }

    @Test
    void testPublishChargeCreated_Success() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("charge.created"), eq("1"), any(ChargeCreatedEvent.class)))
                .thenReturn(future);

        assertDoesNotThrow(() -> notificationEventProducer.publishChargeCreated(chargeCreatedEvent));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChargeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ChargeCreatedEvent.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals("charge.created", topicCaptor.getValue());
        assertEquals("1", keyCaptor.getValue());
        assertEquals(chargeCreatedEvent.getChargeId(), eventCaptor.getValue().getChargeId());
        assertEquals(3, eventCaptor.getValue().getResidentIds().size());
    }

    @Test
    void testPublishChargeCreated_ThrowsException() {
        when(kafkaTemplate.send(anyString(), anyString(), any(ChargeCreatedEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationEventProducer.publishChargeCreated(chargeCreatedEvent);
        });

        assertTrue(exception.getMessage().contains("Error crítico publicando evento de cargo creado"));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(ChargeCreatedEvent.class));
    }

    @Test
    void testPublishPaymentMarkedAsPaid_Success() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("payment.marked.paid"), eq("100"), any(PaymentMarkedAsPaidEvent.class)))
                .thenReturn(future);

        assertDoesNotThrow(() -> notificationEventProducer.publishPaymentMarkedAsPaid(paymentMarkedAsPaidEvent));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PaymentMarkedAsPaidEvent> eventCaptor = ArgumentCaptor.forClass(PaymentMarkedAsPaidEvent.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals("payment.marked.paid", topicCaptor.getValue());
        assertEquals("100", keyCaptor.getValue());
        assertEquals(paymentMarkedAsPaidEvent.getPaymentId(), eventCaptor.getValue().getPaymentId());
        assertEquals(paymentMarkedAsPaidEvent.getUserId(), eventCaptor.getValue().getUserId());
    }

    @Test
    void testPublishPaymentMarkedAsPaid_ThrowsException() {
        when(kafkaTemplate.send(anyString(), anyString(), any(PaymentMarkedAsPaidEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationEventProducer.publishPaymentMarkedAsPaid(paymentMarkedAsPaidEvent);
        });

        assertTrue(exception.getMessage().contains("Error crítico publicando evento de pago marcado como pagado"));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(PaymentMarkedAsPaidEvent.class));
    }

    @Test
    void testEventIdAndTimestampGenerated() {
        assertNotNull(chargeCreatedEvent.getEventId());
        assertNotNull(chargeCreatedEvent.getOccurredAt());
        assertNotNull(paymentMarkedAsPaidEvent.getEventId());
        assertNotNull(paymentMarkedAsPaidEvent.getOccurredAt());
    }
}
