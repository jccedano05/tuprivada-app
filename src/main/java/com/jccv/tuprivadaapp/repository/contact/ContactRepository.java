package com.jccv.tuprivadaapp.repository.contact;

import com.jccv.tuprivadaapp.model.contact.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByResidentId(Long residentId);
    List<Contact> findByCondominiumId(Long condominiumId);
}