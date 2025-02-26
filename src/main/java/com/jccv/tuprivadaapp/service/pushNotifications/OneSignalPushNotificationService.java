package com.jccv.tuprivadaapp.service.pushNotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.model.pushNotification.OneSignalPushNotification;
import com.jccv.tuprivadaapp.repository.pushNotificacion.OneSignalPushNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OneSignalPushNotificationService {

    private final OneSignalPushNotificationRepository pushNotificationRepository;
    private final RestTemplate restTemplate;

    @Value("${onesignal.app-id}")
    private String oneSignalAppId;

    @Value("${onesignal.api-key}")
    private String oneSignalApiKey;

    @Value("${onesignal.android-channel-id}")
    private String oneSignalAndroidChannelId;



    @Autowired
    public OneSignalPushNotificationService(OneSignalPushNotificationRepository pushNotificationRepository, RestTemplate restTemplate) {
        this.pushNotificationRepository = pushNotificationRepository;
        this.restTemplate = restTemplate;
    }



    public void sendPushToResidentsList(List<Long> residentsId ,PushNotificationRequest request) {

        List<OneSignalPushNotification> notifications =
                pushNotificationRepository.findAllByUserIds(residentsId);


        if (notifications.isEmpty()){
            System.out.println("Esta vacio el notifications");
            return;
        }


        // Filtrar los player IDs suscritos
        List<String> playerIds = notifications.stream()
                .map(OneSignalPushNotification::getOneSignalId)
                .collect(Collectors.toList());

        if (playerIds.isEmpty()) {
            System.out.println("No hay player IDs suscritos.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + oneSignalApiKey);
        headers.set("accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construir cuerpo de OneSignal
        String jsonBody = buildOneSignalBody(request, playerIds);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.onesignal.com/notifications?c=push",
                new HttpEntity<>(jsonBody, headers),
                String.class
        );


    }



    public void sendPushToCondominium(Long condominiumId ,PushNotificationRequest request) {

        List<OneSignalPushNotification> notifications =
                pushNotificationRepository.findByCondominiumIdAndResidentRole(condominiumId);


        if (notifications.isEmpty()){
            System.out.println("Esta vacio el notifications");
            return;
        }


        // Filtrar los player IDs suscritos
        List<String> playerIds = notifications.stream()
                .map(OneSignalPushNotification::getOneSignalId)
                .collect(Collectors.toList());

        if (playerIds.isEmpty()) {
            System.out.println("No hay player IDs suscritos.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + oneSignalApiKey);
        headers.set("accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construir cuerpo de OneSignal
        String jsonBody = buildOneSignalBody(request, playerIds);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.onesignal.com/notifications?c=push",
                new HttpEntity<>(jsonBody, headers),
                String.class
        );


    }


    public void sendPushToUser(PushNotificationRequest request) {

        List<OneSignalPushNotification> notifications =
                pushNotificationRepository.findAllByUserId(request.getUserId());

        notifications.forEach(user ->
                System.out.println(user.getOneSignalId()));

        if (notifications.isEmpty()){
            System.out.println("Esta vacio el notifications");
            return;
        }


        // Filtrar los player IDs suscritos
        List<String> playerIds = notifications.stream()
                .map(OneSignalPushNotification::getOneSignalId)
                .collect(Collectors.toList());

        if (playerIds.isEmpty()) {
            System.out.println("No hay player IDs suscritos.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + oneSignalApiKey);
        headers.set("accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construir cuerpo de OneSignal
        String jsonBody = buildOneSignalBody(request, playerIds);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.onesignal.com/notifications?c=push",
                new HttpEntity<>(jsonBody, headers),
                String.class
        );


    }

//    private boolean isPlayerIdSubscribed(String playerId) {
//        try {
//            String url = String.format("https://onesignal.com/api/v1/players/%s?app_id=%s", playerId, oneSignalAppId);
//            System.out.println("player id: " + playerId);
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Basic " + oneSignalApiKey);
//
//            ResponseEntity<String> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    new HttpEntity<>(headers),
//                    String.class
//            );
//
//            // Parsear la respuesta JSON para ver si está suscrito
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> responseBody = mapper.readValue(response.getBody(), Map.class);
//
//            // Verificamos el campo "invalid_identifier" o "active" en la respuesta
//            return responseBody.containsKey("active") && (Boolean) responseBody.get("active");
//
//        } catch (Exception e) {
//            System.out.println("Error checking player ID subscription status: " + e.getMessage());
//            return false; // Si hay un error, asumimos que no está suscrito
//        }
//    }



//    private String buildOneSignalBody(PushNotificationRequest request, List<String> playerIds) {
//        try {
//            // Convertir la lista de player IDs a un array JSON válido
//            String playerIdsJson = new ObjectMapper().writeValueAsString(playerIds);
//
//            // Construir el cuerpo de la solicitud
//            return String.format(
//                    "{"
//                            + "\"app_id\": \"%s\","
//                            + "\"include_aliases\": {\"onesignal_id\": %s},"
//                            + "\"headings\": {\"en\": \"%s\"},"
//                            + "\"contents\": {\"en\": \"%s\"},"
//                            + "\"small_icon\": \"ayni_logo_white\","
//                            + "\"priority\": 50,"
//                            + "\"target_channel\": \"push\""
//                            + "}",
//                    oneSignalAppId,
//                    playerIdsJson, // Usar la lista de player IDs como JSON
//                    request.getTitle(),
//                    request.getMessage()
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Error building JSON body for OneSignal request", e);
//        }
//    }

    private String buildOneSignalBody(PushNotificationRequest request, List<String> playerIds) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> includeAliases = new HashMap<>();
            includeAliases.put("onesignal_id", playerIds);

            Map<String, Object> body = new LinkedHashMap<>();
            // Configuración base
            body.put("app_id", oneSignalAppId);
            body.put("contents", Collections.singletonMap("en", request.getMessage()));
            body.put("headings", Collections.singletonMap("en", request.getTitle()));
            body.put("include_aliases", includeAliases);
            body.put("small_icon", "ayni_logo_white");
            body.put("target_channel", "push");

            // Prioridad máxima para Android
            body.put("priority", 10);  // <-- 10 es máxima prioridad en OneSignal

            // Configuración específica de Android
            body.put("android_visibility", 1); // 1 = Pública (mostrar en pantalla bloqueada)
            body.put("android_channel_id", oneSignalAndroidChannelId); // Nombre de tu canal de alta prioridad

            // Configuración específica de iOS
//            body.put("ios_display_type", "alert"); // Forzar notificación como alerta
//            body.put("ios_badgeType", "Increase");
//            body.put("ios_badgeCount", 1);
//            body.put("mutable_content", true);


            return mapper.writeValueAsString(body);

        } catch (Exception e) {
            throw new RuntimeException("Error building JSON body", e);
        }
    }


    private String mapToJsonString(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

}


