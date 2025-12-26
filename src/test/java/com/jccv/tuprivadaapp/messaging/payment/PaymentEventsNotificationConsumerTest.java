package com.jccv.tuprivadaapp.messaging.payment;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.events.payment.PaymentCompletedEvent;
import com.jccv.tuprivadaapp.service.email.EmailService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventsNotificationConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private OneSignalPushNotificationService pushNotificationService;

    @InjectMocks
    private PaymentEventsNotificationConsumer consumer;

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
    void testHandlePaymentCompleted_Success() {
        doNothing().when(pushNotificationService).sendPushToUser(any(PushNotificationRequest.class));
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString(), anyMap());

        assertDoesNotThrow(() -> consumer.handlePaymentCompleted(testEvent));

        ArgumentCaptor<PushNotificationRequest> pushCaptor = ArgumentCaptor.forClass(PushNotificationRequest.class);
        verify(pushNotificationService, times(1)).sendPushToUser(pushCaptor.capture());

        PushNotificationRequest capturedPush = pushCaptor.getValue();
        assertEquals("¡Pago Exitoso!", capturedPush.getTitle());
        assertEquals("Mantenimiento", capturedPush.getMessage());
        assertEquals(200L, capturedPush.getUserId());

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);

        verify(emailService, times(1)).sendHtmlEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                templateCaptor.capture(),
                variablesCaptor.capture()
        );

        assertEquals("test@example.com", emailCaptor.getValue());
        assertEquals("¡Pago realizado con éxito!", subjectCaptor.getValue());
        assertEquals("payment-success", templateCaptor.getValue());

        Map<String, Object> capturedVariables = variablesCaptor.getValue();
        assertEquals("Juan", capturedVariables.get("nombre"));
        assertEquals("$500.00", capturedVariables.get("monto"));
    }

    @Test
    void testHandlePaymentCompleted_PushNotificationThrowsException() {
        doThrow(new RuntimeException("Push notification failed"))
                .when(pushNotificationService).sendPushToUser(any(PushNotificationRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consumer.handlePaymentCompleted(testEvent);
        });

        assertEquals("Push notification failed", exception.getMessage());
        verify(pushNotificationService, times(1)).sendPushToUser(any(PushNotificationRequest.class));
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void testHandlePaymentCompleted_EmailServiceThrowsException() {
        doNothing().when(pushNotificationService).sendPushToUser(any(PushNotificationRequest.class));
        doThrow(new RuntimeException("Email service failed"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString(), anyMap());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consumer.handlePaymentCompleted(testEvent);
        });

        assertEquals("Email service failed", exception.getMessage());
        verify(pushNotificationService, times(1)).sendPushToUser(any(PushNotificationRequest.class));
        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString(), anyMap());
    }
}
