package com.jccv.tuprivadaapp.dto.condominium;

import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.service.condominium.AddressService;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CondominiumDto {

    @Autowired
    private AddressService addressService;


    private Long id;

    private String name;

    private Long addressId;

    public static Condominium convertToCondominium(CondominiumDto condominiumDto){
        return  Condominium.builder()
                .id(condominiumDto.getId())
                .name(condominiumDto.getName())
                .build();
    }
}
