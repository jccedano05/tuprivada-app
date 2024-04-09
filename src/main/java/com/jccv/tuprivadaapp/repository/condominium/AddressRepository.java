package com.jccv.tuprivadaapp.repository.condominium;

import com.jccv.tuprivadaapp.model.condominium.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
