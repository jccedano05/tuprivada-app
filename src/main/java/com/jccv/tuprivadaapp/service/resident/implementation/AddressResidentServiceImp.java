package com.jccv.tuprivadaapp.service.resident.implementation;

import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import com.jccv.tuprivadaapp.repository.resident.facade.AddressResidentFacade;
import com.jccv.tuprivadaapp.service.resident.AddressResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressResidentServiceImp implements AddressResidentService {

    @Autowired
    private AddressResidentFacade addresResidentFacade;
    @Override
    public AddressResidentDto getAddressResidentById(Long id) {
        return addresResidentFacade.getAddressResidentById(id);
    }

    @Override
    public AddressResidentDto getAddressResidentByResidentId(Long residentId) {
        return addresResidentFacade.getAddressResidentByResidentId(residentId);
    }

    @Override
    public List<AddressResidentDto> getAllAddressResidents() {
        return addresResidentFacade.getAllAddressResident();
    }



    @Override
    public List<AddressResidentDto> getAllAddressResidentsByCondominiumId(Long condominiumId) {
        return addresResidentFacade.getAllResidentByCondominiumId(condominiumId);
    }

    @Override
    public AddressResidentDto createAddressResident(AddressResidentDto addressResidentDto) {
        return addresResidentFacade.createAddressResident(addressResidentDto);
    }
}
