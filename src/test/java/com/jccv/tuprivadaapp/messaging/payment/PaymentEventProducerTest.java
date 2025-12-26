package com.jccv.tuprivadaapp.messaging.payment;

import com.jccv.tuprivadaapp.dto.events.payment.PaymentCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    private PaymentCompletedEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new PaymentCompletedEvent(
                1L,
                100L,
                200L,
                "test@example.com",
                "Juan",
                500.0,
                "MXN",
                "Mantenimiento",
                "Pago de mantenimiento mensual"
        );
    }

    @Test
    void testPublishPaymentCompleted_Success() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("payments.completed"), eq("1"), any(PaymentCompletedEvent.class)))
                .thenReturn(future);

        assertDoesNotThrow(() -> paymentEventProducer.publishPaymentCompleted(testEvent));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PaymentCompletedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals("payments.completed", topicCaptor.getValue());
        assertEquals("1", keyCaptor.getValue());
        assertEquals(testEvent.getPaymentId(), eventCaptor.getValue().getPaymentId());
        assertEquals(testEvent.getUserEmail(), eventCaptor.getValue().getUserEmail());
    }

    @Test
    void testPublishPaymentCompleted_KafkaTemplateThrowsException() {
        when(kafkaTemplate.send(anyString(), anyString(), any(PaymentCompletedEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentEventProducer.publishPaymentCompleted(testEvent);
        });

        assertTrue(exception.getMessage().contains("Error crítico publicando evento de pago completado"));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(PaymentCompletedEvent.class));
    }

    @Test
    void testPublishPaymentCompleted_EventIdIsGenerated() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any(PaymentCompletedEvent.class)))
                .thenReturn(future);

        assertNotNull(testEvent.getEventId());
        assertNotNull(testEvent.getOccurredAt());

        paymentEventProducer.publishPaymentCompleted(testEvent);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(PaymentCompletedEvent.class));
    }
}
