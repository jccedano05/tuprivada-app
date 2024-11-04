package com.jccv.tuprivadaapp.repository.resident.mapper;

import com.jccv.tuprivadaapp.model.resident.AddressResident;
import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import com.jccv.tuprivadaapp.repository.resident.facade.ResidentFacade;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddressResidentMapper {

    private final ResidentFacade residentFacade;

    @Autowired
    public AddressResidentMapper(ResidentFacade residentFacade, CondominiumService condominiumService) {
        this.residentFacade = residentFacade;
    }

    public AddressResidentDto convertModelToDto(AddressResident address){
        return AddressResidentDto.builder()
                .id(address.getId())
                .street(address.getStreet())
                .extNumber(address.getExtNumber())
                .intNumber(address.getIntNumber())
                .intercom(address.getIntercom())
                .residentId(address.getResident().getId())
                .build();
    }

    public AddressResident convertDtoToModel(AddressResidentDto dto){

        return AddressResident.builder()
                .id(dto.getId())
                .street(dto.getStreet())
                .extNumber(dto.getExtNumber())
                .intNumber(dto.getIntNumber())
                .intercom(dto.getIntercom())
                .resident(residentFacade.findResidentById(dto.getResidentId()))
                .build();

    }
}
