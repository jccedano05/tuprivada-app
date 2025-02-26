package com.jccv.tuprivadaapp.controller.pushNotifications;

import com.jccv.tuprivadaapp.dto.pushNotifications.OneSignalPushNotificationDto;
import com.jccv.tuprivadaapp.model.pushNotification.OneSignalPushNotification;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.repository.pushNotificacion.OneSignalPushNotificationRepository;
import com.jccv.tuprivadaapp.service.AuthenticationService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import com.jccv.tuprivadaapp.model.User;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pushNotifications")
public class OneSignalPushNotificationController {

    @Autowired
    private final OneSignalPushNotificationService oneSignalPushNotificationService;

    @Autowired
    private OneSignalPushNotificationRepository oneSignalPushNotificationRepository;

    @Autowired
    private UserFacade userFacade;

    public OneSignalPushNotificationController(OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }

    // Endpoint para guardar el OneSignal ID
//    @PostMapping("/save-onesignal-id")
//    public ResponseEntity<?> saveOneSignalId(@RequestBody OneSignalPushNotificationDto oneSignalPushNotificationDto) {
//
//        OneSignalPushNotification existingNotification = oneSignalPushNotificationRepository
//                .findByOneSignalIdAndSubscriptionId(oneSignalPushNotificationDto.getOneSignalId(),
//                        oneSignalPushNotificationDto.getSubscriptionId());
//
//
//        User user = null;
//
//
//
//        // Si ya existe, no hacemos nada y retornamos una respuesta adecuada
//        if (existingNotification != null  && oneSignalPushNotificationDto.getUserId().equals(existingNotification.getUser().getId())) {
//            return ResponseEntity.ok("OneSignal ID ya está guardado.");
//        }
//
//        if(oneSignalPushNotificationDto.getUserId() != null){
//           user =  userFacade.findById(oneSignalPushNotificationDto.getUserId());
//        }
//
//
//        OneSignalPushNotification notification = OneSignalPushNotification.builder()
//                .oneSignalId(oneSignalPushNotificationDto.getOneSignalId())
//                .user(user)
//                .subscriptionId(oneSignalPushNotificationDto.getSubscriptionId())
//                .build();
//        if(existingNotification != null){
//            notification.setId(existingNotification.getId());
//        }
//        oneSignalPushNotificationRepository.save(notification);
//        return ResponseEntity.ok("OneSignal ID guardado correctamente");
//    }


    @PostMapping("/save-onesignal-id")
    public ResponseEntity<?> saveOneSignalId(@RequestBody OneSignalPushNotificationDto oneSignalPushNotificationDto) {

        OneSignalPushNotification existingNotification = oneSignalPushNotificationRepository
                .findByOneSignalIdAndSubscriptionId(
                        oneSignalPushNotificationDto.getOneSignalId(),
                        oneSignalPushNotificationDto.getSubscriptionId()
                );

        User user = null;

        // Si ya existe un registro, verificamos si el usuario es el mismo o si ambos son nulos.
        if (existingNotification != null) {
            // Caso: Se envía userId nulo y en el registro también es nulo.
            if (oneSignalPushNotificationDto.getUserId() == null && existingNotification.getUser() == null) {
                return ResponseEntity.ok("OneSignal ID ya está guardado.");
            }
            // Caso: Se envía un userId y en el registro el user no es nulo.
            if (oneSignalPushNotificationDto.getUserId() != null && existingNotification.getUser() != null &&
                    oneSignalPushNotificationDto.getUserId().equals(existingNotification.getUser().getId())) {
                return ResponseEntity.ok("OneSignal ID ya está guardado.");
            }
        }

        // Si el userId no es nulo, obtenemos el usuario desde la base de datos.
        if (oneSignalPushNotificationDto.getUserId() != null) {
            user = userFacade.findById(oneSignalPushNotificationDto.getUserId());
        }

        OneSignalPushNotification notification = OneSignalPushNotification.builder()
                .oneSignalId(oneSignalPushNotificationDto.getOneSignalId())
                .user(user)
                .subscriptionId(oneSignalPushNotificationDto.getSubscriptionId())
                .build();

        // Si ya existe el registro, actualizamos su id para realizar un update.
        if (existingNotification != null) {
            notification.setId(existingNotification.getId());
        }

        oneSignalPushNotificationRepository.save(notification);
        return ResponseEntity.ok("OneSignal ID guardado correctamente");
    }



    @GetMapping
//    PostMapping
    public ResponseEntity<?> sendNotification(@RequestBody PushNotificationRequest request) {
        try{

            oneSignalPushNotificationService.sendPushToUser(request);
            return ResponseEntity.ok().build();
    } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (ResourceAccessException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
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

//     Enviar notificación a todos los residentes de un condominio
//    @PostMapping("/send-notification-to-condominium/{condominiumId}")
//    public void sendNotificationToCondominium(@PathVariable Long condominiumId, @RequestBody NotificationRequest notificationRequest) {
//        List<OneSignalPushNotification> notifications = oneSignalPushNotificationRepository.findByUserId(condominiumId);
//
//        for (OneSignalPushNotification notification : notifications) {
//            oneSignalPushNotificationService.sendNotification(notification.getOneSignalId(), notificationRequest.getTitle(), notificationRequest.getBody());
//        }
//    }

//    // Enviar notificación a un usuario específico
//    @PostMapping("/send-notification-to-user/{userId}")
//    public void sendNotificationToUser(@PathVariable Long userId, @RequestBody NotificationRequest notificationRequest) {
//        System.out.println("Entro al /send-notification-to-user/{userId} ");
//        List<OneSignalPushNotification> notifications = oneSignalPushNotificationRepository.findByUserId(userId);
//
//        for (OneSignalPushNotification notification : notifications) {
////            System.out.println("notification");
////            System.out.println(notification.getOneSignalId());
////            System.out.println(notification.getUserId());
//            System.out.println("title");
//            System.out.println(notificationRequest.getTitle());
//            System.out.println("body");
//            System.out.println(notificationRequest.getBody());
//            oneSignalPushNotificationService.sendNotification(notification.getSubscriptionId(), notificationRequest.getTitle(), notificationRequest.getBody());
//        }
//    }

}

