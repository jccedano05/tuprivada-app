package com.jccv.tuprivadaapp.service.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registro centralizado de estrategias de pasarelas para aplicar el patrón Strategy + Factory.
 */
@Component
public class PaymentGatewayRegistry {

    private final Map<PaymentProvider, PaymentGatewayStrategy> strategies = new EnumMap<>(PaymentProvider.class);

    public PaymentGatewayRegistry(Map<PaymentProvider, PaymentGatewayStrategy> strategies) {
        if (strategies != null) {
            this.strategies.putAll(strategies);
        }
    }

    public void registerStrategy(PaymentProvider provider, PaymentGatewayStrategy strategy) {
        strategies.put(provider, strategy);
    }

    public PaymentGatewayStrategy getStrategy(PaymentProvider provider) {
        PaymentGatewayStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("No existe estrategia configurada para el proveedor: " + provider);
        }
        return strategy;
    }

    public boolean hasStrategy(PaymentProvider provider) {
        return strategies.containsKey(provider);
    }
}
