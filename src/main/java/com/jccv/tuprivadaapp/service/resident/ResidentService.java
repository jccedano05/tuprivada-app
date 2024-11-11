package com.jccv.tuprivadaapp.service.resident;

import com.jccv.tuprivadaapp.dto.resident.ResidentDto;
import com.jccv.tuprivadaapp.dto.resident.ResidentRelevantInfoDto;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;

import java.util.List;
import java.util.Optional;

public interface ResidentService {

    public Optional<Resident> getResidentById(Long id);
    public List<Resident> findAllById(List<Long> ids);
    ResidentDto getResidentByUserId(Long userId);
    public Optional<List<Contact>> getAllContactsByResidentId(Long id);
    public Resident saveResident(Resident resident);
    public Resident updateResident(Long id ,Resident resident);
    public void deleteResident(Long id);

    List<ResidentRelevantInfoDto> getAllResidentsWithCondominiumId(Long condominiumId);


    List<Resident> getResidentsByIds(List<Long> residentIds);
}
