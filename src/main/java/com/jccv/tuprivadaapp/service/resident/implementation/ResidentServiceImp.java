package com.jccv.tuprivadaapp.service.resident.implementation;

import com.jccv.tuprivadaapp.model.resident.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.service.AuthenticationService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResidentServiceImp implements ResidentService {

    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Optional<Resident> getResidentById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<List<Contact>> getAllContactsByResidentId(Long id) {
        return residentRepository.findAllContactsByResidentId(id);

    }

    @Override
    public Resident saveResident(Resident resident) {
        return residentRepository.save(resident);
    }



    @Override
    public Resident updateResident(Long id, Resident resident) {
        return null;
    }

    @Override
    public void deleteResident(Long id) {

    }
}
