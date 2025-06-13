package com.jccv.tuprivadaapp.configuration;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class StripeConfig {

    @Value("${stripe.api-key.test}")
    private String stripeApiKeyTest;
    @Value("${stripe.api-key.prod}")
    private String stripeApiKeyProd;
    @Value("${stripe.app.type}")
    private String stripeAppType;



    @Bean
    @Lazy // Inicializa solo cuando se inyecte por primera vez
    public StripeClient stripeClient() {
        return switch (stripeAppType) {
            case "prod" -> new StripeClient(stripeApiKeyProd);
            case "test" -> new StripeClient(stripeApiKeyTest);
            default -> null;
        };
    }
}
