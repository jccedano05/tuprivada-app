package com.jccv.tuprivadaapp.service.contact;

import com.jccv.tuprivadaapp.dto.contact.ContactDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.contact.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactService {


    public List<Contact> findAll();

    public Contact findById(Long id);

    public List<Contact> findByResidentId(Long residentId);

    public List<Contact> findByCondominiumId(Long condominiumId);

    public ContactDto save(ContactDto contactDTO);
    public ContactDto update(Long id, ContactDto contactDTO);

    public void deleteById(Long id);

}
