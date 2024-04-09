package com.jccv.tuprivadaapp.service.resident;

import com.jccv.tuprivadaapp.model.resident.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;

import java.util.List;
import java.util.Optional;

public interface ResidentService {

    public Optional<Resident> getResidentById(Long id);
    public Optional<List<Contact>> getAllContactsByResidentId(Long id);
    public Resident saveResident(Resident resident);
    public Resident updateResident(Long id ,Resident resident);
    public void deleteResident(Long id);
}
