package com.jccv.tuprivadaapp.service.payment;

import com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentSummaryDto;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    public PaymentDto create(PaymentDto paymentDto);
    public List<Payment> createAll(List<Payment> paymentDtos);

    public PaymentDto findById(Long id);

    public List<PaymentDto> findAll();

    public PaymentDto update(PaymentDto paymentDto);

    public void deleteById(Long id);

    public Page<PaymentDetailsDto> getUnpaidPaymentsForResident(Long residentId, int page, int size);

    public Page<PaymentDetailsDto> getPaidPaymentsForResident(Long residentId, int page, int size);

    public Page<PaymentDetailsDto> getPaymentsInRangeForResident(Long residentId, LocalDateTime startDate, LocalDateTime endDate, int page, int size);

    Double getTotalDebtForResident(Long residentId);
    LocalDateTime getRelevantDueDateForResident(Long residentId);


    PaymentSummaryDto getPaymentSummaryForResident(Long residentId);

    public List<Payment> applyChargeToResidents(List<Resident> residents, Charge charge);

}
