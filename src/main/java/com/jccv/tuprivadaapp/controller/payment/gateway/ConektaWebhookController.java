package com.jccv.tuprivadaapp.controller.payment.gateway;

import com.jccv.tuprivadaapp.configuration.ConektaConfig;
import com.jccv.tuprivadaapp.dto.payment.gateway.WebhookProcessResult;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador dedicado para webhooks de Conekta.
 * Procesa eventos de pago en tiempo real desde Conekta.
 */
@RestController
@RequestMapping("/api/v1/webhooks/conekta")
@RequiredArgsConstructor
@Slf4j
public class ConektaWebhookController {
    
    private final PaymentGatewayService gatewayService;
    private final ConektaConfig conektaConfig;
    
    /**
     * Endpoint para recibir webhooks de Conekta
     * Este endpoint debe ser configurado en el dashboard de Conekta
     */
    @PostMapping
    public ResponseEntity<WebhookProcessResult> handleConektaWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Conekta-Signature", required = false) String signature,
            @RequestHeader(value = "X-Conekta-Signature", required = false) String xSignature) {
        
        log.info("[ConektaWebhook] Webhook recibido");
        
        try {
            // Conekta puede enviar la firma en diferentes headers
            String finalSignature = signature != null ? signature : xSignature;
            
            if (finalSignature == null && !conektaConfig.isSkipWebhookSignatureValidation()) {
                log.warn("[ConektaWebhook] Webhook sin firma, rechazado por seguridad");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Firma faltante"));
            }
            
            if (finalSignature == null) {
                log.warn("[ConektaWebhook] Firma omitida por configuración (solo pruebas locales). TODO: reactivar validación en producción.");
            }
            
            // Procesar el webhook
            WebhookProcessResult result = gatewayService.processWebhook(
                    "CONEKTA", payload, finalSignature);
            
            if (result.isProcessed()) {
                log.info("[ConektaWebhook] Webhook procesado exitosamente: {} - {}", 
                        result.getEventType(), result.getTransactionReference());
            } else {
                log.warn("[ConektaWebhook] Webhook recibido pero no procesado: {}", 
                        result.getEventType());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("[ConektaWebhook] Error procesando webhook: {}", e.getMessage(), e);
            
            // Conekta reintentará si devolvemos error, así que registramos pero devolvemos OK
            // para evitar reintentos innecesarios si el error es permanente
            return ResponseEntity.ok(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Endpoint de verificación para Conekta
     * Conekta puede hacer ping a este endpoint para verificar que está activo
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook() {
        log.info("[ConektaWebhook] Verificación de webhook solicitada");
        return ResponseEntity.ok("Webhook activo");
    }
    
    private WebhookProcessResult createErrorResponse(String message) {
        WebhookProcessResult result = new WebhookProcessResult();
        result.setProcessed(false);
        result.setMessage("Error: " + message);
        return result;
    }
}
