package com.jccv.tuprivadaapp.dto.amenity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AmenityDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal cost;
    private Boolean isActive;
    private Long condominiumId;  // Relación con Condominium
}
