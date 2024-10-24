package com.jccv.tuprivadaapp.repository.resident.facade;

import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.resident.PaymentResident;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.PaymentResidentRepository;
import com.jccv.tuprivadaapp.repository.resident.dto.PaymentResidentDto;
import com.jccv.tuprivadaapp.repository.resident.mapper.PaymentResidentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentResidentFacade {

    @Autowired
    private PaymentResidentRepository paymentResidentRepository;

    @Autowired
    private ResidentFacade residentFacade;

    @Autowired
    private PaymentResidentMapper paymentResidentMapper;


    public PaymentResident findPaymentById(Long paymentId){
        return paymentResidentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("referencia de pago no encontrado"));
    }

    public List<PaymentResidentDto> findAllPaymentsByResidentId(Long residentId){
        List<PaymentResident> paymentsResident = paymentResidentRepository.findAllPaymentResidentByResidentId(residentId).orElseThrow(() -> new ResourceNotFoundException("pago de residente no encontrado"));
        return paymentsResident.stream().map(payment -> paymentResidentMapper.paymentModelToDto(payment)).collect(Collectors.toList());
    }

    public List<PaymentResidentDto> findAllPaymentsByCondominiumId(Long condominiumId){
        List<PaymentResident> paymentsResident = paymentResidentRepository.findAllPaymentResidentByCondominiumId(condominiumId).orElseThrow(() -> new ResourceNotFoundException("pago de residente no encontrado"));
        return paymentsResident.stream().map(payment -> paymentResidentMapper.paymentModelToDto(payment)).collect(Collectors.toList());
    }


    public PaymentResidentDto save(PaymentResidentDto paymentDto){
        if(paymentDto.getResidentId() == null){
            throw new BadRequestException("El residentId no debe ser nulo");
        }

        Resident  resident = residentFacade.findResidentById(paymentDto.getResidentId());
        PaymentResident paymentResident = paymentResidentMapper.paymentDtoToModel(paymentDto, resident);
        paymentResident = paymentResidentRepository.save(paymentResident);
        return paymentResidentMapper.paymentModelToDto(paymentResident);
    }


}
