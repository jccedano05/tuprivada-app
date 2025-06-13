package com.jccv.tuprivadaapp.controller.stripe;

import com.jccv.tuprivadaapp.dto.stripe.CreateAccountRequest;
import com.jccv.tuprivadaapp.dto.stripe.StripeAccountResponse;
import com.jccv.tuprivadaapp.exception.StripeServiceException;
import com.jccv.tuprivadaapp.service.stripe.StripeAccountService;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeAccountController {

    private final StripeAccountService service;

    @Autowired
    public StripeAccountController(StripeAccountService service) {
        this.service = service;
    }
    // constructor

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest req) throws StripeException {
        try{
            StripeAccountResponse account = service.createAccount(req);
        return ResponseEntity.ok(account);
    }catch (StripeServiceException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }catch (Exception e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }


    @GetMapping("/accounts/{id}/link")
    public ResponseEntity<String> accountLink(@PathVariable String id) throws StripeException {
        try{
            String url = service.generateAccountLink(id);
            return ResponseEntity.ok(url);
        }catch (StripeServiceException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<Map<String,String>> refreshLink(@RequestParam String accountId) {
        // llama a tu generateAccountLink real
        String newLink = service.generateAccountLink(accountId);
        Map<String,String> body = Collections.singletonMap("url", newLink);
        return ResponseEntity.ok(body);
    }

    /**
     * 2) Complete endpoint: simula la pantalla final tras onboarding
     *    GET /api/stripe/onboarding/complete?accountId=acct_123
     */
    @GetMapping("/complete")
    public ResponseEntity<Map<String,Object>> completeOnboarding(@RequestParam String accountId) {
        // aquí podrías en el futuro consultar Stripe.retrieve(accountId)
        Map<String,Object> body = new HashMap<>();
        body.put("accountId", accountId);
        body.put("status", "completed");
        body.put("message", "Onboarding finalizado correctamente");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/reauth")
    public ResponseEntity<Map<String,String>> reauth(@RequestParam String accountId) {
        // Vuelve a invocar a Stripe para generar un nuevo link
        String newLink = service.generateAccountLink(accountId);
        Map<String,String> body = Collections.singletonMap("url", newLink);
        return ResponseEntity.ok(body);
    }


}

