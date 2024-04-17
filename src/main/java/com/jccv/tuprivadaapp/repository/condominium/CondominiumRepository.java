package com.jccv.tuprivadaapp.repository.condominium;

import com.jccv.tuprivadaapp.model.condominium.Condominium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CondominiumRepository extends JpaRepository<Condominium, Long> {
}
