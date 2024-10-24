package com.jccv.tuprivadaapp.service.resident.implementation;

import com.jccv.tuprivadaapp.model.resident.PaymentResident;
import com.jccv.tuprivadaapp.repository.resident.dto.PaymentResidentDto;
import com.jccv.tuprivadaapp.repository.resident.facade.PaymentResidentFacade;
import com.jccv.tuprivadaapp.repository.resident.mapper.PaymentResidentMapper;
import com.jccv.tuprivadaapp.service.resident.PaymentResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentResidentServiceImp implements PaymentResidentService {


    @Autowired
    private PaymentResidentFacade paymentFacade;
    @Autowired
    private PaymentResidentMapper paymentResidentMapper;
    @Override
    public PaymentResidentDto getPaymentResidentById(Long id) {
        PaymentResident payment = paymentFacade.findPaymentById(id);
        PaymentResidentDto dto = paymentResidentMapper.paymentModelToDto(payment);
        return dto;
    }

    @Override
    public List<PaymentResidentDto> getAllPaymentsByResidentId(Long residentId) {
        List<PaymentResidentDto> paymentsByResidentDto = paymentFacade.findAllPaymentsByResidentId(residentId);
        return paymentsByResidentDto;
    }

    @Override
    public List<PaymentResidentDto> getAllDebtsPaymentByResidentId(Long residentId) {
        return null;
    }

    @Override
    public List<PaymentResidentDto> getAllPaymentsByCondominiumId(Long condominiumId) {

        List<PaymentResidentDto> paymentsByResidentDto = paymentFacade.findAllPaymentsByCondominiumId(condominiumId);
        return paymentsByResidentDto;
    }

    @Override
    public PaymentResidentDto savePaymentResident(PaymentResidentDto paymentResidentDto) {
        return paymentFacade.save(paymentResidentDto);
    }

    @Override
    public PaymentResidentDto updatePaymentResident(Long id, PaymentResidentDto paymentResidentDto ) {
        return null;
    }

    @Override
    public void deletePaymentResident(Long id) {

    }
}
