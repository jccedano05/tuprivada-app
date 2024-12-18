package com.jccv.tuprivadaapp.service.resident;

import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;

import java.util.List;

public interface AddressResidentService {

    public AddressResidentDto getAddressResidentById(Long id);
    public AddressResidentDto getAddressResidentByResidentId(Long residentId);

    public List<AddressResidentDto>getAllAddressResidents();
    public List<AddressResidentDto>getAllAddressResidentsByCondominiumId(Long condominiumId);

    public AddressResidentDto createAddressResident(AddressResidentDto addressResidentDto);


}
