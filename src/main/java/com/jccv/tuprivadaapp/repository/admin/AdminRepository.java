package com.jccv.tuprivadaapp.repository.admin;

import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

        Optional<Admin> findByUserId(Long userId);

    }
