package com.jccv.tuprivadaapp.repository.condominium;

import com.jccv.tuprivadaapp.model.condominium.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

//    @Query("""
//        select c from Contact c inner join Resident r on c.resident.id = c.id
//        where c.resident.id = :id
//        """)
    Optional<Address> findByCondominiumId(Long condominiumId);

}
