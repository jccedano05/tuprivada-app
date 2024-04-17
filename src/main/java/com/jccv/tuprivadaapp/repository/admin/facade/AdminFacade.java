package com.jccv.tuprivadaapp.repository.admin.facade;

import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.worker.Worker;
import com.jccv.tuprivadaapp.repository.admin.AdminRepository;
import com.jccv.tuprivadaapp.repository.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminFacade {
    @Autowired
    private AdminRepository adminRepository;

    public Admin findAdminById(Long adminId){
        return adminRepository.findById(adminId).orElseThrow(() -> new ResourceNotFoundException("Admin no encontrado con el id: " + adminId));
    }

    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }
}
