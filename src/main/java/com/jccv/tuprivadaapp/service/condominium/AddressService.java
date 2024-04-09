package com.jccv.tuprivadaapp.service.condominium;

import com.jccv.tuprivadaapp.dto.condominium.AddressDto;
import com.jccv.tuprivadaapp.model.condominium.Address;

import java.util.List;

public interface AddressService {
    public Address create(AddressDto address);

    public Address findById(Long id);

    public List<Address> findAll();

    public Address update(AddressDto address);

    public void deleteById(Long id);

}
