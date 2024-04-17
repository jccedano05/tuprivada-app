package com.jccv.tuprivadaapp.repository.resident;

import com.jccv.tuprivadaapp.model.resident.Contact;
import com.jccv.tuprivadaapp.model.resident.PaymentResident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;

@Repository
public interface PaymentResidentRepository extends JpaRepository<PaymentResident, Long> {

    Optional<List<PaymentResident>> findAllPaymentResidentByResidentId(Long residentId);


    @Query("SELECT pr FROM PaymentResident pr JOIN User u ON pr.resident.id = u.id WHERE u.condominium.id = :condominiumId")
    Optional<List<PaymentResident>> findAllPaymentResidentByCondominiumId(@Param("condominiumId") Long condominiumId);

}

