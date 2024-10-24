package com.jccv.tuprivadaapp.dto.admin.mapper;


import com.jccv.tuprivadaapp.dto.admin.AdminDto;
import com.jccv.tuprivadaapp.model.admin.Admin;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public AdminDto toDTO(Admin admin) {
        AdminDto dto = new AdminDto();
        dto.setId(admin.getId());
        dto.setActive(admin.isActive());
        dto.setUserId(admin.getUser() != null ? admin.getUser().getId() : null);
        return dto;
    }

    public Admin toEntity(AdminDto dto) {
        Admin admin = new Admin();
        admin.setId(dto.getId());
        admin.setActive(dto.isActive());
        return admin;
    }
}
