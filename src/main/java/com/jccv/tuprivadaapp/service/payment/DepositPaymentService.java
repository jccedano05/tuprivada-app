package com.jccv.tuprivadaapp.service.payment;

import com.jccv.tuprivadaapp.dto.payment.DepositPaymentDto;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface DepositPaymentService {
    DepositPaymentDto addDeposit(Long paymentId, DepositPaymentDto depositPaymentDTO);
    List<DepositPaymentDto> getDepositsByPayment(Long paymentId);

    Double getTotalDepositsAmountByPaymentId(Long paymentId);
    List<DepositPaymentDto> getDepositsByResidentAndDateRange(Long residentId, LocalDateTime startDate, LocalDateTime endDate);

    @Transactional
    DepositPaymentDto updateDeposit(Long depositId, DepositPaymentDto updatedDto);

    @Transactional
    void deleteDeposit(Long depositId);

    @Transactional
    void deleteAllDepositsByPaymentId(Long paymentId);

    @Transactional
    void deleteAllDepositsWithBalanceUpdateByPaymentId(Long paymentId, Resident resident);
}
