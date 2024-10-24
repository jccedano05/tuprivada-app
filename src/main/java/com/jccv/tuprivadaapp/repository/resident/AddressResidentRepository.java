package com.jccv.tuprivadaapp.repository.resident;

import com.jccv.tuprivadaapp.model.resident.AddressResident;
import com.jccv.tuprivadaapp.model.resident.PaymentResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressResidentRepository extends JpaRepository<AddressResident, Long> {


    @Query("SELECT ar FROM AddressResident ar WHERE ar.condominium.id = :condominiumId")
    Optional<List<AddressResident>> findAllAddressResidentByCondominiumId(@Param("condominiumId") Long condominiumId);
}
