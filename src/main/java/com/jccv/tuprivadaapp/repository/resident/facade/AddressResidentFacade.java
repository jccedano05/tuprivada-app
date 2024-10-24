package com.jccv.tuprivadaapp.repository.resident.facade;

import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.model.resident.AddressResident;
import com.jccv.tuprivadaapp.repository.resident.AddressResidentRepository;
import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import com.jccv.tuprivadaapp.repository.resident.mapper.AddressResidentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddressResidentFacade {

    @Autowired
    private AddressResidentRepository residentRepository;
    @Autowired
    private AddressResidentMapper addressResidentMapper;

    public AddressResidentDto getAddresResidentById(Long id) {
        AddressResident address = residentRepository.findById(id).orElseThrow(() -> new BadRequestException("No se pudo encontrar el domicilio con el id: " + id));
        return addressResidentMapper.convertModelToDto(address);
    }

    public List<AddressResidentDto> getAllAddressResident() {
        List<AddressResident> addresses = residentRepository.findAll();
        return addresses.stream().map(address -> addressResidentMapper.convertModelToDto(address)).collect(Collectors.toList());
    }

    public List<AddressResidentDto> getAllResidentByCondominiumId(Long condominiumId) {
        List<AddressResident> addresses = residentRepository.findAllAddressResidentByCondominiumId(condominiumId).orElseThrow(() -> new BadRequestException("No se pudo encontrar el domicilios con el id: " + condominiumId));
        return addresses.stream().map(address -> addressResidentMapper.convertModelToDto(address)).collect(Collectors.toList());
    }

    public AddressResidentDto createAddressResident(AddressResidentDto addressResidentDto) {
        AddressResident address = addressResidentMapper.convertDtoToModel(addressResidentDto);
         address = residentRepository.save(address);
        return addressResidentMapper.convertModelToDto(address);
    }
}
