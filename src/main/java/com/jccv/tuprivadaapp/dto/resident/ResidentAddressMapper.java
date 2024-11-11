package com.jccv.tuprivadaapp.dto.resident;

import com.jccv.tuprivadaapp.model.resident.AddressResident;
import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import org.springframework.stereotype.Component;

@Component
public class ResidentAddressMapper {

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
}
