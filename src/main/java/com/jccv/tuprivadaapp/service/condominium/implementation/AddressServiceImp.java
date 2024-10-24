package com.jccv.tuprivadaapp.service.condominium.implementation;


import com.jccv.tuprivadaapp.dto.condominium.AddressDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.condominium.AddressRepository;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.service.condominium.AddressService;
import com.jccv.tuprivadaapp.utils.UserSessionInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImp implements AddressService {

    private final AddressRepository addressRepository;

    private final CondominiumRepository condominiumRepository;

    private final UserSessionInformation userSessionInformation;

    @Autowired
    public AddressServiceImp(AddressRepository addressRepository, CondominiumRepository condominiumRepository, UserSessionInformation userSessionInformation) {
        this.addressRepository = addressRepository;
        this.condominiumRepository = condominiumRepository;
        this.userSessionInformation = userSessionInformation;
    }

    public Address create(AddressDto addressDto) {
        User user = userSessionInformation.getUserInformationFromSecurityContext();
        //Only Superadmin or Admin with the same condominium can create
        if(user.getRole() != Role.SUPERADMIN ){
            if(user.getCondominium().getId() != addressDto.getCondominiumId()){

                throw new BadRequestException("Address can't create ");
            }
        }

        Optional<Address> addressFounded =  addressRepository.findByCondominiumId(addressDto.getCondominiumId());
        if(addressFounded.isPresent()){
            throw new BadRequestException("Ya existe condominio ligado a esa direccion");
        }

        Condominium condominium = condominiumRepository.findById(addressDto.getCondominiumId()).orElseThrow(()-> new ResourceNotFoundException("Condominio no encontrado con el id: " + addressDto.getCondominiumId()));
        Address address = AddressDto.convertToAddress(addressDto);
        address.setCondominium(condominium);
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
        Address address = AddressDto.convertToAddress(addressDto);
        address.setCondominium(condominium);
        return addressRepository.save(address);
    }

    public void deleteById(Long id) {
        addressRepository.deleteById(id);
    }
}
