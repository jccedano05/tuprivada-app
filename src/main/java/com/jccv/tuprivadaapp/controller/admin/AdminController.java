package com.jccv.tuprivadaapp.controller.admin;

import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.repository.admin.AdminRepository;
import com.jccv.tuprivadaapp.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admins")
public class AdminController {

    private final AdminRepository adminRepository;
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminRepository adminRepository, AdminService adminService) {
        this.adminRepository = adminRepository;
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        Admin adminSaved = adminRepository.save(admin);
        return ResponseEntity.ok(adminSaved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAdminByUserId(@PathVariable Long userId) {
        try {
            return new ResponseEntity<>(adminService.getAdminByUserId(userId), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
