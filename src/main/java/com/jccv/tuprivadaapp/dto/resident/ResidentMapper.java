package com.jccv.tuprivadaapp.dto.resident;

import com.jccv.tuprivadaapp.dto.auth.UserDto;
import com.jccv.tuprivadaapp.dto.auth.mapper.UserMapper;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.stereotype.Component;

@Component
public class ResidentMapper {


    private final ResidentAddressMapper residentAddressMapper;
    private final UserDto userDto;
    private final UserMapper userMapper;

    public ResidentMapper(ResidentAddressMapper residentAddressMapper, UserDto userDto, UserMapper userMapper) {
        this.residentAddressMapper = residentAddressMapper;
        this.userDto = userDto;
        this.userMapper = userMapper;
    }


    public  ResidentDto toDTO(Resident resident) {

        ResidentDto dto = new ResidentDto();
        dto.setId(resident.getId());
        dto.setActiveResident(resident.isActiveResident());
        dto.setUserId(resident.getUser() != null ? resident.getUser().getId() : null);
        return dto;
    }

    public ResidentRelevantInfoDto modelToResidentInfoDto(Resident resident){

        ResidentRelevantInfoDto dto = new ResidentRelevantInfoDto();
        dto.setId(resident.getId());
        dto.setActiveResident(resident.isActiveResident());

        if(resident.getUser() != null){
            dto.setUserDto(userMapper.convertUserToUserDto(resident.getUser()));
        }

        if(resident.getAddressResident() != null){
            dto.setAddressResidentDto(residentAddressMapper.convertModelToDto(resident.getAddressResident()));
        }
        return dto;
    }

    public  Resident toEntity(ResidentDto dto) {

        Resident resident = new Resident();
        resident.setId(dto.getId());
        resident.setActiveResident(dto.isActiveResident());


        return resident;
    }
}
