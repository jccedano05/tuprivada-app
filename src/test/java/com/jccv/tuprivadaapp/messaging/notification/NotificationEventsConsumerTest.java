package com.jccv.tuprivadaapp.messaging.notification;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.events.charge.ChargeCreatedEvent;
import com.jccv.tuprivadaapp.dto.events.payment.PaymentMarkedAsPaidEvent;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventsConsumerTest {

    @Mock
    private OneSignalPushNotificationService pushNotificationService;

    @InjectMocks
    private NotificationEventsConsumer consumer;

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
    void testHandleChargeCreated_Success() {
        doNothing().when(pushNotificationService).sendPushToResidentsList(anyList(), any(PushNotificationRequest.class));

        assertDoesNotThrow(() -> consumer.handleChargeCreated(chargeCreatedEvent));

        ArgumentCaptor<List<Long>> residentIdsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<PushNotificationRequest> pushCaptor = ArgumentCaptor.forClass(PushNotificationRequest.class);

        verify(pushNotificationService, times(1)).sendPushToResidentsList(
                residentIdsCaptor.capture(),
                pushCaptor.capture()
        );

        assertEquals(3, residentIdsCaptor.getValue().size());
        assertEquals("Nuevo cargo", pushCaptor.getValue().getTitle());
        assertTrue(pushCaptor.getValue().getMessage().contains("Mantenimiento"));
    }

    @Test
    void testHandleChargeCreated_EmptyResidentList() {
        ChargeCreatedEvent emptyEvent = new ChargeCreatedEvent(
                1L,
                "Mantenimiento",
                "Test",
                500.0,
                Collections.emptyList()
        );

        assertDoesNotThrow(() -> consumer.handleChargeCreated(emptyEvent));

        verify(pushNotificationService, never()).sendPushToResidentsList(anyList(), any(PushNotificationRequest.class));
    }

    @Test
    void testHandleChargeCreated_NullResidentList() {
        ChargeCreatedEvent nullEvent = new ChargeCreatedEvent(
                1L,
                "Mantenimiento",
                "Test",
                500.0,
                null
        );

        assertDoesNotThrow(() -> consumer.handleChargeCreated(nullEvent));

        verify(pushNotificationService, never()).sendPushToResidentsList(anyList(), any(PushNotificationRequest.class));
    }

    @Test
    void testHandleChargeCreated_ThrowsException() {
        doThrow(new RuntimeException("Push notification failed"))
                .when(pushNotificationService).sendPushToResidentsList(anyList(), any(PushNotificationRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consumer.handleChargeCreated(chargeCreatedEvent);
        });

        assertEquals("Push notification failed", exception.getMessage());
        verify(pushNotificationService, times(1)).sendPushToResidentsList(anyList(), any(PushNotificationRequest.class));
    }

    @Test
    void testHandlePaymentMarkedAsPaid_Success() {
        doNothing().when(pushNotificationService).sendPushToUser(any(PushNotificationRequest.class));

        assertDoesNotThrow(() -> consumer.handlePaymentMarkedAsPaid(paymentMarkedAsPaidEvent));

        ArgumentCaptor<PushNotificationRequest> pushCaptor = ArgumentCaptor.forClass(PushNotificationRequest.class);

        verify(pushNotificationService, times(1)).sendPushToUser(pushCaptor.capture());

        PushNotificationRequest capturedPush = pushCaptor.getValue();
        assertEquals("¡Pago Exitoso!", capturedPush.getTitle());
        assertEquals("Mantenimiento", capturedPush.getMessage());
        assertEquals(300L, capturedPush.getUserId());
    }

    @Test
    void testHandlePaymentMarkedAsPaid_ThrowsException() {
        doThrow(new RuntimeException("Push notification failed"))
                .when(pushNotificationService).sendPushToUser(any(PushNotificationRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consumer.handlePaymentMarkedAsPaid(paymentMarkedAsPaidEvent);
        });

        assertEquals("Push notification failed", exception.getMessage());
        verify(pushNotificationService, times(1)).sendPushToUser(any(PushNotificationRequest.class));
    }
}
