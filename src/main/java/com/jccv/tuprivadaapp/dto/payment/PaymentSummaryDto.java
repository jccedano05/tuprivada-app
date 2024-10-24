package com.jccv.tuprivadaapp.dto.payment;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PaymentSummaryDto {

    private Double totalDebt;
    private LocalDateTime nextDueDate;
    private AccountBankDto accountBankDto;
}