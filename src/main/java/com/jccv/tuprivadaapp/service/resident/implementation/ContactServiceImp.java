package com.jccv.tuprivadaapp.service.resident.implementation;

import com.jccv.tuprivadaapp.model.resident.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.ContactRepository;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.service.resident.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ContactServiceImp implements ContactService {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ResidentRepository residentRepository;


    @Override
    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }
    @Override
    public Optional<List<Contact>> getAllContactsByResidentId(Long id) {
        return contactRepository.getAllContactsByResidentId(id);
    }

    @Override
    public Contact saveContact(Long idResident, Contact contact) {
//        Resident resident = residentRepository.findById(idResident).orElseThrow(NoSuchElementException::new);
//        contact.setResident(resident);
//        return contactRepository.save(contact);
        return null;
    }


    @Override
    public Contact updateContact(Long id, Contact contact) {

//        Resident resident = residentRepository.findById(id).orElseThrow(NoSuchElementException::new);
//        Contact contactFounded = contactRepository.findById(contact.getId()).orElseThrow(NoSuchElementException::new);
return null;
    }

    @Override
    public void deleteContact(Long id) {

    }
}

