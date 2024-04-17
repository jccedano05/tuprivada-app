package com.jccv.tuprivadaapp.repository.resident;

import com.jccv.tuprivadaapp.model.resident.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

//    @Query("""
//        select c from Contact c inner join Resident r on c.resident.id = c.id
//        where c.resident.id = :id
//        """)
    @Query("""
        select c from Contact c where c.resident.id = :id
        """)
    Optional<List<Contact>> getAllContactsByResidentId(Long id);
}


