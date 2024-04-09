package com.jccv.tuprivadaapp.service.resident;

import com.jccv.tuprivadaapp.model.resident.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactService {


    public Optional<Contact> getContactById(Long id);
    public Optional<List<Contact>> getAllContactsByResidentId(Long id);
    public Contact saveContact(Long idResident,Contact contact);
    public Contact updateContact(Long id ,Contact contact);
    public void deleteContact(Long id);

}
