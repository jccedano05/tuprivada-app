package com.jccv.tuprivadaapp.repository.admin;

import com.jccv.tuprivadaapp.model.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
