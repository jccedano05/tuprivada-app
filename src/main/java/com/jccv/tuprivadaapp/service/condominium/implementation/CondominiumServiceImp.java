package com.jccv.tuprivadaapp.service.condominium.implementation;

import com.jccv.tuprivadaapp.dto.condominium.CondominiumDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.service.condominium.AddressService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CondominiumServiceImp implements CondominiumService {

    @Autowired
    private CondominiumRepository condominiumRepository;

    @Autowired
    private AddressService addressService;



    public Condominium create(CondominiumDto condominiumDto) {
        Condominium condominium = CondominiumDto.convertToCondominium(condominiumDto);
        if (condominiumDto.getAddressId() != null){
            Address address = addressService.findById(condominiumDto.getAddressId());
            condominium.setAddress(address);
        }
        return condominiumRepository.save(condominium);
    }

    public Condominium findById(Long id) {
        return condominiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condominium not found with id: " + id));
    }

    public List<Condominium> findAll() {
        return condominiumRepository.findAll();
    }

    public Condominium update(CondominiumDto condominiumDto) {

        Condominium condominium = CondominiumDto.convertToCondominium(condominiumDto);
        if (condominiumDto.getAddressId() != null){
            Address address = addressService.findById(condominiumDto.getAddressId());
            condominium.setAddress(address);
        }
        return condominiumRepository.save(condominium);
    }

    public void deleteById(Long id) {
        condominiumRepository.deleteById(id);
    }
}
