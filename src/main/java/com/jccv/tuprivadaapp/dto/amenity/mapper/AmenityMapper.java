package com.jccv.tuprivadaapp.dto.amenity.mapper;

import com.jccv.tuprivadaapp.dto.amenity.AmenityDto;
import com.jccv.tuprivadaapp.model.amenity.Amenity;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import org.springframework.stereotype.Component;

@Component
public class AmenityMapper {

    public com.jccv.tuprivadaapp.dto.amenity.AmenityDto toDto(Amenity amenity) {
        return AmenityDto.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .cost(amenity.getCost())
                .isActive(amenity.getIsActive())
                .condominiumId(amenity.getCondominium().getId())
                .build();
    }

    public Amenity toEntity(AmenityDto amenityDto, Condominium condominium) {
        return Amenity.builder()
                .id(amenityDto.getId())
                .name(amenityDto.getName())
                .description(amenityDto.getDescription())
                .cost(amenityDto.getCost())
                .condominium(condominium)
                .isActive(amenityDto.getIsActive())
                .build();
    }
}
