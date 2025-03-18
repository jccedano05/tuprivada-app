package com.jccv.tuprivadaapp.service.amenity.implementation;


import com.jccv.tuprivadaapp.dto.amenity.AmenityDto;
import com.jccv.tuprivadaapp.dto.amenity.mapper.AmenityMapper;
import com.jccv.tuprivadaapp.model.amenity.Amenity;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.amenity.AmenityRepository;
import com.jccv.tuprivadaapp.service.amenity.AmenityService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmenityServiceImp implements AmenityService {

    private final AmenityRepository amenityRepository;

    private final CondominiumService condominiumService;

    private final AmenityMapper amenityMapper;

    @Autowired
    public AmenityServiceImp(AmenityRepository amenityRepository, CondominiumService condominiumService, AmenityMapper amenityMapper) {
        this.amenityRepository = amenityRepository;
        this.condominiumService = condominiumService;
        this.amenityMapper = amenityMapper;
    }

    @Override
    public AmenityDto createAmenity(AmenityDto amenityDto) {
        Condominium condominium = condominiumService.findById(amenityDto.getCondominiumId());

        Amenity amenity = amenityMapper.toEntity(amenityDto, condominium);
        amenity.setCondominium(condominium);
        amenity.setIsActive(true); // Por defecto, la amenidad se crea activa

        Amenity savedAmenity = amenityRepository.save(amenity);
        return amenityMapper.toDto(savedAmenity);
    }

    @Override
    public AmenityDto updateAmenity(Long id, AmenityDto amenityDto) {
        Amenity existingAmenity = amenityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not found"));

        existingAmenity.setName(amenityDto.getName());
        existingAmenity.setDescription(amenityDto.getDescription());
        existingAmenity.setCost(amenityDto.getCost());
        existingAmenity.setIsActive(amenityDto.getIsActive());

        Amenity updatedAmenity = amenityRepository.save(existingAmenity);
        return amenityMapper.toDto(updatedAmenity);
    }

    @Override
    public AmenityDto getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not found"));
        return amenityMapper.toDto(amenity);
    }

    @Override
    public List<AmenityDto> getAllAmenities(Long condominiumId) {
        List<Amenity> amenities = amenityRepository.findByCondominiumId(condominiumId);
        return amenities.stream().map(amenityMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteAmenity(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not found"));
        amenityRepository.delete(amenity);
    }
}
