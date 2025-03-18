package com.jccv.tuprivadaapp.controller.amenity;

import com.jccv.tuprivadaapp.dto.amenity.AmenityDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.amenity.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;

@RestController
@RequestMapping("/api/amenities")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @PostMapping
    public ResponseEntity<?> createAmenity(@RequestBody AmenityDto amenityDto) {
        try{
            AmenityDto createdAmenity = amenityService.createAmenity(amenityDto);
            return ResponseEntity.ok(createdAmenity);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAmenity(@PathVariable Long id, @RequestBody AmenityDto amenityDto) {
        try{
            AmenityDto updatedAmenity = amenityService.updateAmenity(id, amenityDto);
            return ResponseEntity.ok(updatedAmenity);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAmenityById(@PathVariable Long id) {
        AmenityDto amenity = amenityService.getAmenityById(id);
        return ResponseEntity.ok(amenity);
    }

    @GetMapping("/condominium/{condominiumId}")
    public ResponseEntity<List<AmenityDto>> getAllAmenities(@PathVariable Long condominiumId) {
        List<AmenityDto> amenities = amenityService.getAllAmenities(condominiumId);
        return ResponseEntity.ok(amenities);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
