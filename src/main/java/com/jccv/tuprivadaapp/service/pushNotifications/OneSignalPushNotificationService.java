package com.jccv.tuprivadaapp.service.pushNotifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OneSignalPushNotificationService {

    @Value("${onesignal.app-id}")
    private String appId;

    @Value("${onesignal.api-key}")
    private String apiKey;

//    private static final String ONESIGNAL_API_URL = "https://onesignal.com/api/v1/notifications";
    private static final String ONESIGNAL_API_URL = "https://api.onesignal.com/notifications?c=push";

    public void sendNotification(String oneSignalId, String title, String message) {
        RestTemplate restTemplate = new RestTemplate();

        // Crear los headers de la solicitud HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + apiKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        System.out.println(oneSignalId);
        // Crear el cuerpo de la solicitud
        String payload =  "{"
                + "\"app_id\": \"" + appId + "\","
                + "\"include_player_ids\": [\"" + oneSignalId + "\"],"
                + "\"headings\": {\"en\": \"" + title + "\"},"
                + "\"contents\": {\"en\": \"" + message + "\"}"
                + "}";

//        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity(ONESIGNAL_API_URL, entity, String.class);
//        System.out.println("Respuesta de OneSignal: " + response.getBody());
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(ONESIGNAL_API_URL, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Notificación enviada exitosamente: " + response.getBody());
            } else {
                System.out.println("Error al enviar la notificación. Código: " + response.getStatusCode());
                System.out.println("Respuesta: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error al enviar la notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
