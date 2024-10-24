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
    private String nameCommunity;
    private String nameSuburb;
    private String state;
    private Integer postalCode;

    private Long condominiumId;


    public static Address convertToAddress(AddressDto addressDto){
        return  Address.builder()
                .id(addressDto.getId())
                .street(addressDto.getStreet())
                .number(addressDto.getNumber())
                .nameCommunity(addressDto.getNameCommunity())
                .nameSuburb(addressDto.getNameSuburb())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .postalCode(addressDto.getPostalCode())
                .build();
    }
}
