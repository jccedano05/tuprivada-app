package com.jccv.tuprivadaapp.dto.condominium.mapper;

import com.jccv.tuprivadaapp.dto.condominium.CondominiumDto;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import org.springframework.stereotype.Component;

@Component
public class CondominiumMapper {
    public Condominium convertToCondominium(CondominiumDto condominiumDto){
        return  Condominium.builder()
                .id(condominiumDto.getId())
                .name(condominiumDto.getName())
                .build();
    }
}
