package com.jccv.tuprivadaapp.service.screens.mobile;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.dto.screens.mobile.PaymentsScreenDto;
import com.jccv.tuprivadaapp.dto.screens.mobile.payments.ResidentAccountPaymentDetailsDto;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.utils.UserSessionInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentsScreenService {


    private final UserSessionInformation userSessionInformation;
    private final AccountBankService accountBankService;

    @Autowired
    public PaymentsScreenService(UserSessionInformation userSessionInformation, AccountBankService accountBankService) {
        this.userSessionInformation = userSessionInformation;
        this.accountBankService = accountBankService;
    }


    public PaymentsScreenDto getResidentPaymetsScreenInformation(){

      User user = userSessionInformation.getUserInformationFromSecurityContext();
        System.out.println("****** USER ************");

        System.out.println("username: " + user.getUsername());
        System.out.println("condominiumId: " + user.getCondominium().getId());

        AccountBankDto accountBankDto = accountBankService.findAccountBankByCondominium(user.getCondominium().getId());
        System.out.println("accountBankDto: " + accountBankDto.getBankName());

        ResidentAccountPaymentDetailsDto residentAccountPaymentDetailsDto = ResidentAccountPaymentDetailsDto.builder()
                .balance(600)
                .paymentDueDate("16/oct/24")
                .accountBankDto(accountBankDto)
                .build();

        System.out.println("********* DESP DE Condominium service *******");

      PaymentsScreenDto paymentsScreenDto = PaymentsScreenDto.builder()
              .residentAccountPaymentDetailsDto(residentAccountPaymentDetailsDto)
              .build();

        return paymentsScreenDto;
    }

}
