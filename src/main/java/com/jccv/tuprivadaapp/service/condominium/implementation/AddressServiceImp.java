package com.jccv.tuprivadaapp.service.condominium.implementation;


import com.jccv.tuprivadaapp.dto.condominium.AddressDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.condominium.AddressRepository;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.service.condominium.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImp implements AddressService {
    @Autowired
    private  AddressRepository addressRepository;
    @Autowired
    private CondominiumRepository condominiumRepository;

    public Address create(AddressDto addressDto) {
        Condominium condominium = condominiumRepository.findById(addressDto.getCondominiumId()).orElseThrow(()-> new ResourceNotFoundException("Condominium not found"));
        Address address = AddressDto.convertToAddress(addressDto, condominium);
        return addressRepository.save(address);
    }

    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    }

    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    public Address update(AddressDto addressDto) {
        Condominium condominium = condominiumRepository.findById(addressDto.getCondominiumId()).orElseThrow(()-> new ResourceNotFoundException("Condominium not found"));
        addressRepository.findById(addressDto.getId()).orElseThrow(()-> new ResourceNotFoundException("Address not found"));
        Address address = AddressDto.convertToAddress(addressDto, condominium);
        return addressRepository.save(address);
    }

    public void deleteById(Long id) {
        addressRepository.deleteById(id);
    }
}
