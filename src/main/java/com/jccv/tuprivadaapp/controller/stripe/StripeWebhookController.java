package com.jccv.tuprivadaapp.controller.stripe;


import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.service.stripe.StripePaymentsService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Scanner;

@RestController
@RequestMapping("/stripe-webhooks")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final StripePaymentsService stripePaymentsService;

    @Autowired
    public StripeWebhookController(StripePaymentsService stripePaymentsService) {
        this.stripePaymentsService = stripePaymentsService;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {

        try{
            String payload;
            try (Scanner scanner = new Scanner(request.getInputStream(), "UTF-8")) {
                payload = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
            }

            String sigHeader = request.getHeader("Stripe-Signature");
            Event event;

            try {
                event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            } catch (SignatureVerificationException e) {
                // Firma no válida
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma no valida para construir el ");
            }
            System.out.println("Event API version: " + event.getApiVersion());
            System.out.println("Library API version: " + Stripe.API_VERSION);
            // Manejar el evento
            if ("payment_intent.succeeded".equals(event.getType())) {
                System.out.println("Entro al Succeeded");
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);
                if (paymentIntent != null) {
                    stripePaymentsService.handlePaymentIntentSucceeded(paymentIntent);
                }
            }

            return ResponseEntity.ok("");
        }catch(BadRequestException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        }
}