package com.jccv.tuprivadaapp.service.resident;

import com.jccv.tuprivadaapp.dto.resident.ResidentChargeSummaryDto;
import com.jccv.tuprivadaapp.dto.resident.ResidentDto;
import com.jccv.tuprivadaapp.dto.resident.ResidentRelevantInfoDto;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ResidentService {

    public Optional<Resident> getResidentById(Long id);
    ResidentRelevantInfoDto getResidentDtoById(Long residentId);

    public List<Resident> findAllById(List<Long> ids);
    ResidentDto getResidentByUserId(Long userId);
    public Optional<List<Contact>> getAllContactsByResidentId(Long id);
    public Resident saveResident(Resident resident);
    public Resident updateResident(Long id ,Resident resident);
    public void deleteResident(Long id);

    List<ResidentRelevantInfoDto> getAllResidentsWithCondominiumId(Long condominiumId);


    List<Resident> getResidentsByIds(List<Long> residentIds);

    public void updateBalanceResident(Long residentId, Double amountToInclude);
    public void updateBalanceResident(Resident resident, Double amountToInclude);
    public List<Resident> getAllByCondominiumId(Long condominiumId);

    List<ResidentChargeSummaryDto> getAllResidentsChargesSummariesByCondominiumId(Long condominiumId);

    Double getBalanceOfResidentById(Long id);
}
