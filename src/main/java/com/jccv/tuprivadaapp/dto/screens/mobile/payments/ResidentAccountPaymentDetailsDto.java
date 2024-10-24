package com.jccv.tuprivadaapp.dto.screens.mobile.payments;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResidentAccountPaymentDetailsDto {

    private AccountBankDto accountBankDto;
    private Integer balance;;
    private String paymentDueDate;
}
