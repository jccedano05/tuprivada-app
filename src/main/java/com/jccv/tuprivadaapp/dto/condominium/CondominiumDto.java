package com.jccv.tuprivadaapp.dto.condominium;

import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CondominiumDto {


    private Long id;
    public static Condominium convertToCondominium(CondominiumDto condominiumDto){
        return  Condominium.builder()
                .id(condominiumDto.getId())
                .build();
    }
}
