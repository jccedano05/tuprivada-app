package com.jccv.tuprivadaapp.dto.resident;

import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.mapper.AddressResidentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResidentMapper {




    public  ResidentDto toDTO(Resident resident) {

        ResidentDto dto = new ResidentDto();
        dto.setId(resident.getId());
        dto.setActiveResident(resident.isActiveResident());
        dto.setUserId(resident.getUser() != null ? resident.getUser().getId() : null);

        return dto;
    }

    public  Resident toEntity(ResidentDto dto) {

        Resident resident = new Resident();
        resident.setId(dto.getId());
        resident.setActiveResident(dto.isActiveResident());


        return resident;
    }
}
