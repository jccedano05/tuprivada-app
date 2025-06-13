package com.jccv.tuprivadaapp.repository.resident;

import com.jccv.tuprivadaapp.dto.resident.ResidentChargeSummaryDto;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    @Query("SELECT new com.jccv.tuprivadaapp.dto.resident.ResidentChargeSummaryDto(" +
            "  r.id,                                         " + // residentId
            "  r.user.firstName,                             " + // firstName
            "  r.user.lastName,                              " + // lastName
            "  COALESCE(ar.street, ''),                      " + // street (puede ser NULL)
            "  COALESCE(ar.extNumber, ''),                   " + // extNumber (puede ser NULL)
            "  SUM(CASE WHEN p.isPaid = true  AND p.isDeleted = false THEN 1 ELSE 0 END),  " + // paidPayments → Long
            "  SUM(CASE WHEN p.isPaid = false AND p.isDeleted = false THEN 1 ELSE 0 END),  " + // unpaidPayments → Long
            "  SUM(CASE WHEN p.isPaid = false AND p.isDeleted = false THEN p.charge.amount ELSE 0 END) " + // totalDue → Double
            ") " +
            "FROM Resident r " +
            "LEFT JOIN r.addressResident ar   " +  // residente puede no tener dirección
            "LEFT JOIN r.payments p           " +  // residente puede no tener pagos aún
            "WHERE r.condominium.id = :condominiumId " +  // filtramos por condominio
            "GROUP BY r.id, r.user.firstName, r.user.lastName, ar.street, ar.extNumber")
    List<ResidentChargeSummaryDto> getAllResidentsChargesSummariesByCondominiumId(
            @Param("condominiumId") Long condominiumId
    );
}
