package com.jccv.tuprivadaapp.configuration;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount.PaymentProvider;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayRegistry;
import com.jccv.tuprivadaapp.service.payment.gateway.impl.ConektaPaymentGatewayStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuración central para registro de estrategias de pasarelas de pago.
 * Registra automáticamente las estrategias disponibles en el sistema.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayConfiguration {
    
    private final PaymentGatewayRegistry gatewayRegistry;
    private final ConektaPaymentGatewayStrategy conektaStrategy;
    
    @PostConstruct
    public void registerStrategies() {
        // Registrar estrategia de Conekta
        gatewayRegistry.registerStrategy(PaymentProvider.CONEKTA, conektaStrategy);
        log.info("[PaymentGateway] Estrategia CONEKTA registrada exitosamente");
        
        // Aquí se pueden registrar otras estrategias en el futuro
        // gatewayRegistry.registerStrategy(PaymentProvider.STRIPE, stripeStrategy);
        // gatewayRegistry.registerStrategy(PaymentProvider.OPENPAY, openpayStrategy);
        
        log.info("[PaymentGateway] {} estrategias de pago registradas", 
                PaymentProvider.values().length);
    }
}
