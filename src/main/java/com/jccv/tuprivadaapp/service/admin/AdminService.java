package com.jccv.tuprivadaapp.service.admin;

import com.jccv.tuprivadaapp.dto.admin.AdminDto;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.contact.Contact;

import java.util.List;
import java.util.Optional;

public interface AdminService {

    Optional<Admin> getAdminById(Long id);
    List<Admin> findAllById(List<Long> ids);
    AdminDto getAdminByUserId(Long userId);

    Admin saveAdmin(Admin admin);
    Admin updateAdmin(Long id, Admin admin);
    void deleteAdmin(Long id);
}
