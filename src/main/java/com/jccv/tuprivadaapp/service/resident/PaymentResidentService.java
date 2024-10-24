package com.jccv.tuprivadaapp.service.resident;

import com.jccv.tuprivadaapp.repository.resident.dto.PaymentResidentDto;

import java.util.List;

public interface PaymentResidentService {

    public PaymentResidentDto getPaymentResidentById(Long id);
    public List<PaymentResidentDto> getAllPaymentsByResidentId(Long residentId);
    public List<PaymentResidentDto> getAllDebtsPaymentByResidentId(Long residentId);
    public List<PaymentResidentDto> getAllPaymentsByCondominiumId(Long condominiumId);
    public PaymentResidentDto savePaymentResident(PaymentResidentDto paymentResidentDto );
    public PaymentResidentDto updatePaymentResident(Long id ,PaymentResidentDto paymentResidentDto );
    public void deletePaymentResident(Long id);
}
