package com.jccv.tuprivadaapp.service.payment.gateway;

import com.jccv.tuprivadaapp.dto.payment.gateway.AccountCreationRequest;
import com.jccv.tuprivadaapp.dto.payment.gateway.AccountCreationResponse;
import com.jccv.tuprivadaapp.dto.payment.gateway.AccountInfoResponse;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;

import java.util.List;

/**
 * Servicio para administrar cuentas de pasarelas de pago por condominio.
 */
public interface PaymentGatewayAccountService {

    PaymentGatewayAccount createOrUpdateAccount(Long condominiumId, PaymentGatewayAccount account);

    PaymentGatewayAccount activateAccount(Long accountId);

    PaymentGatewayAccount deactivateAccount(Long accountId, String reason);

    PaymentGatewayAccount getActiveAccount(Long condominiumId, PaymentGatewayAccount.PaymentProvider provider);

    PaymentGatewayAccount getPreferredAccount(Long condominiumId);

    List<PaymentGatewayAccount> getAccounts(Long condominiumId);

    AccountCreationResponse initiateAccountCreation(AccountCreationRequest request) throws com.jccv.tuprivadaapp.dto.payment.gateway.PaymentGatewayException;

    AccountInfoResponse fetchAccountInfo(Long accountId) throws com.jccv.tuprivadaapp.dto.payment.gateway.PaymentGatewayException;

    boolean isAccountReadyForPayments(Long condominiumId);
    
    PaymentGatewayAccount getOrCreateDefaultAccount(Long condominiumId, PaymentGatewayAccount.PaymentProvider provider);
}
