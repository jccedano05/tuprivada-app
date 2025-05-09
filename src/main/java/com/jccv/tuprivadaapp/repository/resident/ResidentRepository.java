package com.jccv.tuprivadaapp.repository.resident;

import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    Optional<Resident> findByUserId(Long userId);

    @Query("""
        select c from Contact c inner join Resident r on c.resident.id = c.id
        where c.resident.id = :id
        """)
    Optional<List<Contact>> findAllContactsByResidentId(Long id);

    // Ordenar por calle y luego por número exterior
    @Query("SELECT r FROM Resident r WHERE r.condominium.id = :condominiumId AND r.isActiveResident = true " +
            "ORDER BY LOWER(r.addressResident.street), r.addressResident.extNumber")
    List<Resident> findAllByCondominiumIdAndIsActiveResidentTrueOrderByStreetAndExtNumberIgnoreCase(Long condominiumId);

}
