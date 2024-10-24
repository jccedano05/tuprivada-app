package com.jccv.tuprivadaapp.service.resident.implementation;

import com.jccv.tuprivadaapp.dto.resident.ResidentDto;
import com.jccv.tuprivadaapp.dto.resident.ResidentMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResidentServiceImp implements ResidentService {

    private final ResidentMapper residentMapper;

    private final ResidentRepository residentRepository;;

    @Autowired
    public ResidentServiceImp(ResidentMapper residentMapper, ResidentRepository residentRepository) {
        this.residentMapper = residentMapper;
        this.residentRepository = residentRepository;
    }

    @Override
    public Optional<Resident> getResidentById(Long id) {
        return residentRepository.findById(id);
    }

    @Override
    public List<Resident> findAllById(List<Long> ids) {
        return residentRepository.findAllById(ids);
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
    public ResidentDto getResidentByUserId(Long userId) {
        Resident resident = residentRepository.findByUserId(userId).orElseThrow(()->new ResourceNotFoundException("Resident not found with userId:" + userId));
        return residentMapper.toDTO(resident);
    }

    @Override
    public Resident updateResident(Long id, Resident resident) {
        return null;
    }

    @Override
    public void deleteResident(Long id) {

    }
}
