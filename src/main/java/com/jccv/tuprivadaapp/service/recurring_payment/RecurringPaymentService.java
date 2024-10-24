package com.jccv.tuprivadaapp.service.recurring_payment;


import com.jccv.tuprivadaapp.dto.recurringPayment.RecurringPaymentDto;

import java.util.List;

public interface RecurringPaymentService {
    public RecurringPaymentDto create(RecurringPaymentDto paymentDto);

    public RecurringPaymentDto findById(Long id);

    public List<RecurringPaymentDto> findAll();

    public RecurringPaymentDto update(RecurringPaymentDto paymentDto);

    public void deleteById(Long id);
}
