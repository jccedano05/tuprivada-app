package com.jccv.tuprivadaapp.repository.condominium;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CondominiumRepository extends JpaRepository<Condominium, Long> {
    @Query("SELECT u FROM User u WHERE u.condominium.id = :condominiumId AND u.role = 'RESIDENT'")
    List<User> findUserWithRoleResidentsByCondominiumId(Long condominiumId);
}
