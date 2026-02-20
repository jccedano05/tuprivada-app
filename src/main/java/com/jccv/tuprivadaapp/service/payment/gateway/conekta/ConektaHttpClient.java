package com.jccv.tuprivadaapp.service.payment.gateway.conekta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.configuration.ConektaConfig;
import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Cliente HTTP para interactuar con la API de Conekta.
 * Implementa mejores prácticas empresariales:
 * - Manejo robusto de errores
 * - Retry automático con backoff exponencial
 * - Logging detallado para auditoría
 * - Timeout configurables
 * - Serialización/deserialización segura
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConektaHttpClient {

    private final ConektaConfig conektaConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String CONEKTA_API_BASE_URL = "https://api.conekta.io";
    private static final String ORDERS_ENDPOINT = "/orders";
    private static final String CONEKTA_API_VERSION = "2.2.0";
    
    /**
     * Crea una orden en Conekta
     * 
     * @param orderRequest Datos de la orden en formato Map
     * @return JsonNode con la respuesta de Conekta
     * @throws PaymentGatewayException Si hay error en la comunicación o procesamiento
     */
    public JsonNode createOrder(Map<String, Object> orderRequest) throws PaymentGatewayException {
        try {
            log.info("[ConektaHttpClient] Creando orden en Conekta API");
            log.debug("[ConektaHttpClient] Request body: {}", maskSensitiveData(orderRequest));
            
            String requestBody = objectMapper.writeValueAsString(orderRequest);
            
            String response = webClient.post()
                    .uri(ORDERS_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader())
                    .header(HttpHeaders.ACCEPT, "application/vnd.conekta-v" + CONEKTA_API_VERSION + "+json")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, conektaConfig.getLocale())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("Conekta-Client-User-Agent", "TuPrivada-App/1.0 (Java; Spring Boot)")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("[ConektaHttpClient] Error 4xx de Conekta: {}", errorBody);
                                return Mono.error(new PaymentGatewayException(
                                    "Error de cliente en Conekta: " + errorBody,
                                    "CONEKTA_CLIENT_ERROR"
                                ));
                            })
                    )
                    .onStatus(
                        status -> status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("[ConektaHttpClient] Error 5xx de Conekta: {}", errorBody);
                                return Mono.error(new PaymentGatewayException(
                                    "Error del servidor de Conekta: " + errorBody,
                                    "CONEKTA_SERVER_ERROR"
                                ));
                            })
                    )
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5))
                        .filter(throwable -> !(throwable instanceof PaymentGatewayException))
                        .doBeforeRetry(retrySignal -> 
                            log.warn("[ConektaHttpClient] Reintentando petición (intento {}): {}", 
                                retrySignal.totalRetries() + 1, 
                                retrySignal.failure().getMessage())
                        )
                    )
                    .timeout(Duration.ofMillis(conektaConfig.getReadTimeout()))
                    .block();
            
            JsonNode responseNode = objectMapper.readTree(response);
            log.info("[ConektaHttpClient] Orden creada exitosamente: {}", responseNode.path("id").asText());
            log.debug("[ConektaHttpClient] Response completo: {}", maskSensitiveData(responseNode));
            
            return responseNode;
            
        } catch (WebClientResponseException e) {
            log.error("[ConektaHttpClient] Error HTTP al crear orden: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentGatewayException(
                "Error HTTP de Conekta: " + e.getResponseBodyAsString(),
                "CONEKTA_HTTP_ERROR_" + e.getStatusCode().value(),
                e
            );
        } catch (Exception e) {
            log.error("[ConektaHttpClient] Error inesperado al crear orden: {}", e.getMessage(), e);
            throw new PaymentGatewayException(
                "Error inesperado al comunicarse con Conekta: " + e.getMessage(),
                "CONEKTA_UNEXPECTED_ERROR",
                e
            );
        }
    }
    
    /**
     * Construye el header de autorización Basic con la API key
     */
    private String buildAuthorizationHeader() {
        String apiKey = conektaConfig.getPrivateKey() != null && !conektaConfig.getPrivateKey().isEmpty()
            ? conektaConfig.getPrivateKey()
            : conektaConfig.getApiKey();
            
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Conekta API key no configurada");
        }
        
        // Conekta usa autenticación Basic con API key como usuario y contraseña vacía
        String credentials = apiKey + ":";
        String encodedCredentials = Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        
        return "Basic " + encodedCredentials;
    }
    
    /**
     * Enmascara datos sensibles en logs (emails, teléfonos, etc.)
     */
    private Object maskSensitiveData(Object data) {
        // Por ahora retorna el objeto tal cual
        // TODO: Implementar lógica de enmascaramiento si es necesario
        return data;
    }
}
