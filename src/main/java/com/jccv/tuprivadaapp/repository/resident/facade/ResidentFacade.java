package com.jccv.tuprivadaapp.repository.resident.facade;

import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResidentFacade {

    @Autowired
    private ResidentRepository residentRepository;

    public Resident findResidentById(Long residentId){
        return residentRepository.findById(residentId).orElseThrow(() -> new ResourceNotFoundException("referencia de pago no encontrado"));
    }

    public Resident save(Resident resident) {
        return residentRepository.save(resident);
    }
}
