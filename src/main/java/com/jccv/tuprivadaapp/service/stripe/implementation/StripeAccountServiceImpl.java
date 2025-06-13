package com.jccv.tuprivadaapp.service.stripe.implementation;

import com.jccv.tuprivadaapp.dto.stripe.CreateAccountRequest;
import com.jccv.tuprivadaapp.dto.stripe.StripeAccountResponse;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.StripeServiceException;
import com.jccv.tuprivadaapp.service.stripe.StripeAccountService;
import com.stripe.Stripe;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;




@Service
public class StripeAccountServiceImpl implements StripeAccountService {

    @Value("${stripe.api-key.test}") private String stripeApiKeyTest;
    @Value("${stripe.api-key.prod}") private String stripeApiKeyProd;
    @Value("${stripe.app.type}")
    private String stripeAppType;

    @PostConstruct
    public void init() {

         switch (stripeAppType) {
            case "prod" -> Stripe.apiKey = stripeApiKeyProd ;
            case "test" -> Stripe.apiKey = stripeApiKeyTest;
            default -> throw new BadRequestException("stripeAppType environment is not correct");
        };
    }

    public StripeAccountResponse createAccount(CreateAccountRequest dto){
        try{
//            AccountCreateParams params = AccountCreateParams.builder()
//                    .setType(AccountCreateParams.Type.EXPRESS)
//                    .setEmail(dto.getEmail())
//                    .build();
//            return Account.create(params);


            Account account = Account.create(AccountCreateParams.builder().build());
            // Mapear solo los campos necesarios
            return new StripeAccountResponse(
                    account.getId(),
                    account.getEmail(),
                    account.getChargesEnabled()
            );
        }catch (StripeException e) {
            throw new StripeServiceException("Error creando cuenta Express en Stripe", e);
        }
    }

    public String generateAccountLink(String accountId) {
        try{
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl("http://localhost:8080/reauth")
                    .setReturnUrl("http://localhost:8080/return")
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();
            return AccountLink.create(params).getUrl();
        }catch (StripeException e) {
            throw new StripeServiceException("Error creando cuenta Express en Stripe", e);
        }
    }



}
