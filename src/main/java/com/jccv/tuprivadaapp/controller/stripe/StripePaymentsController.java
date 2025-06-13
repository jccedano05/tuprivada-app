package com.jccv.tuprivadaapp.controller.stripe;


import com.jccv.tuprivadaapp.dto.stripe.StripePaymentIntentResponse;
import com.jccv.tuprivadaapp.dto.stripe.StripePaymentRequest;
import com.jccv.tuprivadaapp.exception.StripeServiceException;
import com.jccv.tuprivadaapp.service.stripe.StripePaymentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.stripe.model.PaymentIntent;

@RestController
@RequestMapping("/api/v1/stripe-payments")
public class StripePaymentsController {

    private final StripePaymentsService stripePaymentsService;

    @Autowired
    public StripePaymentsController(StripePaymentsService stripePaymentsService) {
        this.stripePaymentsService = stripePaymentsService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPayment(@RequestBody StripePaymentRequest request) {

        try{
            StripePaymentIntentResponse paymentIntent = stripePaymentsService.createPaymentCardIntent(request);
            return new ResponseEntity<>(paymentIntent, HttpStatus.OK);
        }catch (StripeServiceException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
