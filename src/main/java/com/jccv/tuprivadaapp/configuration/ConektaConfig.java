package com.jccv.tuprivadaapp.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Configuración para la integración con Conekta.
 * Maneja API keys, ambientes y configuración del cliente SDK.
 */
@Component
@ConfigurationProperties(prefix = "conekta")
@PropertySource("classpath:application-conekta.properties")
@Data
@Slf4j
public class ConektaConfig {
    
    private String apiKey;
    private String privateKey;
    private String publicKey;
    private String webhookKey;
    private String environment = "sandbox"; // sandbox | production
    private String locale = "es";
    private String apiVersion = "2.2.0";
    private int connectTimeout = 30000; // 30 segundos
    private int readTimeout = 30000;
    private boolean debugMode = false;
    /**
     * Flag temporal para desactivar la validación de firmas mientras se prueba localmente.
     * TODO: reactivar validación en cuanto se cuenten con firmas reales de Conekta.
     */
    private boolean skipWebhookSignatureValidation = true;
    
    // Comisiones y tarifas
    private Double platformFeePercentage = 2.9; // 2.9%
    private Double platformFeeFixed = 3.00; // $3.00 MXN
    private Double taxPercentage = 16.0; // IVA 16%
    
    // URLs de retorno para checkout
    private String successUrl;
    private String failureUrl;
    private String webhookUrl;
    
    @PostConstruct
    public void init() {
        validateConfiguration();
        log.info("[Conekta] Configuración inicializada y lista para usar WebClient");
    }
    
    /**
     * Valida que la configuración mínima esté presente
     */
    private void validateConfiguration() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("[Conekta] API Key no configurada. La integración con Conekta no funcionará correctamente.");
            // No lanzar excepción para permitir que la aplicación inicie en modo de desarrollo/test
            return;
        }
        
        if (privateKey == null || privateKey.isEmpty()) {
            log.warn("[Conekta] Private Key no configurada. Algunas funciones podrían no estar disponibles.");
        }
        
        if (webhookKey == null || webhookKey.isEmpty()) {
            log.warn("[Conekta] Webhook Key no configurada. No se podrán validar webhooks de forma segura.");
        }
        
        log.info("[Conekta] Configuración inicializada - Ambiente: {}, Locale: {}, API Version: {}", 
                environment, locale, apiVersion);
    }
    
    /**
     * Configura WebClient para comunicación HTTP con Conekta API
     * Implementa mejores prácticas empresariales:
     * - Timeouts configurables
     * - Connection pooling
     * - Logging de requests/responses
     */
    @Bean
    public WebClient conektaWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(connectTimeout, TimeUnit.MILLISECONDS))
            );
        
        String baseUrl = "production".equalsIgnoreCase(environment)
            ? "https://api.conekta.io"
            : "https://api.conekta.io";
        
        WebClient client = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
        
        log.info("[Conekta] WebClient configurado para ambiente: {} - URL: {}", environment, baseUrl);
        
        return client;
    }
    

    
    /**
     * Determina si está en modo producción
     */
    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }
    
    /**
     * Calcula las comisiones para un monto dado
     */
    public Double calculatePlatformFee(Double amount) {
        if (amount == null || amount <= 0) {
            return 0.0;
        }
        return (amount * platformFeePercentage / 100) + platformFeeFixed;
    }
    
    /**
     * Calcula el IVA sobre las comisiones
     */
    public Double calculateTax(Double feeAmount) {
        if (feeAmount == null || feeAmount <= 0) {
            return 0.0;
        }
        return feeAmount * taxPercentage / 100;
    }
}
