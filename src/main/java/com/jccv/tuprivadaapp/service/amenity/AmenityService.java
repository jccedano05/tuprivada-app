package com.jccv.tuprivadaapp.service.amenity;



import com.jccv.tuprivadaapp.dto.amenity.AmenityDto;

import java.util.List;

public interface AmenityService {
    AmenityDto createAmenity(AmenityDto amenityDto);
    AmenityDto updateAmenity(Long id, AmenityDto amenityDto);
    AmenityDto getAmenityById(Long id);
    List<AmenityDto> getAllAmenities(Long condominiumId);
    void deleteAmenity(Long id);
}
