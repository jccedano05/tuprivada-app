package com.jccv.tuprivadaapp.service.admin.implementation;

import com.jccv.tuprivadaapp.dto.admin.AdminDto;
import com.jccv.tuprivadaapp.dto.admin.mapper.AdminMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.repository.admin.AdminRepository;
import com.jccv.tuprivadaapp.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImp implements AdminService {

    private final AdminMapper adminMapper;
    private final AdminRepository adminRepository;

    @Autowired
    public AdminServiceImp(AdminMapper adminMapper, AdminRepository adminRepository) {
        this.adminMapper = adminMapper;
        this.adminRepository = adminRepository;
    }

    @Override
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    @Override
    public List<Admin> findAllById(List<Long> ids) {
        return adminRepository.findAllById(ids);
    }

    @Override
    public AdminDto getAdminByUserId(Long userId) {
        Admin admin = adminRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with userId:" + userId));
        return adminMapper.toDTO(admin);
    }


    @Override
    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    @Override
    public Admin updateAdmin(Long id, Admin admin) {
        return null; // Implementar lógica de actualización si es necesario
    }

    @Override
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}
