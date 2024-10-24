package com.jccv.tuprivadaapp.controller.pushNotifications;

import com.jccv.tuprivadaapp.dto.pushNotifications.OneSignalPushNotificationDto;
import com.jccv.tuprivadaapp.model.pushNotification.OneSignalPushNotification;
import com.jccv.tuprivadaapp.repository.pushNotificacion.OneSignalPushNotificationRepository;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pushNotifications")
public class OneSignalPushNotificationController {

    private final OneSignalPushNotificationService oneSignalPushNotificationService;

    @Autowired
    private OneSignalPushNotificationRepository oneSignalPushNotificationRepository;

    public OneSignalPushNotificationController(OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }

    // Endpoint para guardar el OneSignal ID
    @PostMapping("/save-onesignal-id")
    public ResponseEntity<?> saveOneSignalId(@RequestBody OneSignalPushNotificationDto oneSignalPushNotificationDto) {

        OneSignalPushNotification existingNotification = oneSignalPushNotificationRepository
                .findByOneSignalIdAndSubscriptionId(oneSignalPushNotificationDto.getOneSignalId(),
                        oneSignalPushNotificationDto.getSubscriptionId());


        // Si ya existe, no hacemos nada y retornamos una respuesta adecuada
        if (existingNotification != null) {
            return ResponseEntity.ok("OneSignal ID ya está guardado.");
        }

        OneSignalPushNotification notification = OneSignalPushNotification.builder()
                .oneSignalId(oneSignalPushNotificationDto.getOneSignalId())
                .userId(oneSignalPushNotificationDto.getUserId())
                .subscriptionId(oneSignalPushNotificationDto.getSubscriptionId())
                .build();
        oneSignalPushNotificationRepository.save(notification);
        return ResponseEntity.ok("OneSignal ID guardado correctamente");
    }

//    @PostMapping("/send")
//    public ResponseEntity<String> sendNotification(
//            @RequestBody OneSignalPushNotificationDto notificationRequest) {
//
//        oneSignalPushNotificationService.sendNotificationToUsers(
//                notificationRequest.getHeading(),
//                notificationRequest.getContent(),
//                notificationRequest.getUserIds()
//        );
//
//        return ResponseEntity.ok("Notificación enviada");
//    }

    // Enviar notificación a todos los residentes de un condominio
    @PostMapping("/send-notification-to-condominium/{condominiumId}")
    public void sendNotificationToCondominium(@PathVariable Long condominiumId, @RequestBody NotificationRequest notificationRequest) {
        List<OneSignalPushNotification> notifications = oneSignalPushNotificationRepository.findByUserId(condominiumId);

        for (OneSignalPushNotification notification : notifications) {
            oneSignalPushNotificationService.sendNotification(notification.getOneSignalId(), notificationRequest.getTitle(), notificationRequest.getBody());
        }
    }

    // Enviar notificación a un usuario específico
    @PostMapping("/send-notification-to-user/{userId}")
    public void sendNotificationToUser(@PathVariable Long userId, @RequestBody NotificationRequest notificationRequest) {
        System.out.println("Entro al /send-notification-to-user/{userId} ");
        List<OneSignalPushNotification> notifications = oneSignalPushNotificationRepository.findByUserId(userId);

        for (OneSignalPushNotification notification : notifications) {
//            System.out.println("notification");
//            System.out.println(notification.getOneSignalId());
//            System.out.println(notification.getUserId());
            System.out.println("title");
            System.out.println(notificationRequest.getTitle());
            System.out.println("body");
            System.out.println(notificationRequest.getBody());
            oneSignalPushNotificationService.sendNotification(notification.getSubscriptionId(), notificationRequest.getTitle(), notificationRequest.getBody());
        }
    }

}

// DTO para la notificación
@Data
class NotificationRequest {
    private String title;
    private String body;

    // Getters y Setters
}
