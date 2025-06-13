package com.jccv.tuprivadaapp.service.condominium.implementation;


import com.jccv.tuprivadaapp.dto.condominium.CondominiumDto;
import com.jccv.tuprivadaapp.dto.condominium.mapper.CondominiumMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import com.jccv.tuprivadaapp.service.condominium.AddressService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CondominiumServiceImp implements CondominiumService {

    private final CondominiumRepository condominiumRepository;

    private final AddressService addressService;

    private final CondominiumMapper condominiumMapper;


    @Autowired
    public CondominiumServiceImp(CondominiumRepository condominiumRepository, AddressService addressService, AccountBankService accountBankService, CondominiumDto condominiumDto, CondominiumMapper condominiumMapper) {
        this.condominiumRepository = condominiumRepository;
        this.addressService = addressService;
        this.condominiumMapper = condominiumMapper;
    }


    public Condominium create(CondominiumDto condominiumDto) {
        Condominium condominium = condominiumMapper.convertToCondominium(condominiumDto);
        if (condominiumDto.getAddressId() != null){
            Address address = addressService.findById(condominiumDto.getAddressId());
            condominium.setAddress(address);
        }
        System.out.println("Condominium Service 2");
        System.out.println(condominiumDto.toString());
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

        Condominium condominium = condominiumMapper.convertToCondominium(condominiumDto);
        if (condominiumDto.getAddressId() != null){
            Address address = addressService.findById(condominiumDto.getAddressId());
            condominium.setAddress(address);
        }
        return condominiumRepository.save(condominium);
    }

    @Override
    public String findConnectedAccountIdByCondominiumId(Long condominiumId) {
        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el condominio con el id: " + condominiumId));

        if (condominium.getConnectedAccountId() == null) {
            throw new BadRequestException("El conodominio no tiene asociada una cuenta para pagos en linea (ConnectedAccountId)");
        }

        return condominium.getConnectedAccountId();

    }

    public void deleteById(Long id) {
        condominiumRepository.deleteById(id);
    }


}
