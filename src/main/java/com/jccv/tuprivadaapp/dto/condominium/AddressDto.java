package com.jccv.tuprivadaapp.dto.condominium;

import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AddressDto {

    private Long id;

    private String street;
    private String number;
    private String city;
    private String state;
    private String postalCode;

    private Long condominiumId;


    public static Address convertToAddress(AddressDto addressDto, Condominium condominium){
        return  Address.builder()
                .id(addressDto.getId())
                .street(addressDto.getStreet())
                .number(addressDto.getNumber())
                .city(addressDto.getNumber())
                .state(addressDto.getState())
                .postalCode(addressDto.getPostalCode())
                .condominium(condominium)
                .build();
    }
}
