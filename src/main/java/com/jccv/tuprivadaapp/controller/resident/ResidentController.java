package com.jccv.tuprivadaapp.controller.resident;

import com.jccv.tuprivadaapp.model.resident.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("residents")
public class ResidentController {

    @Autowired
    private ResidentRepository residentRepository;


    @PostMapping
    public ResponseEntity<Resident> createResident(@RequestBody Resident resident){
        //Hacer un DTO y guardar como Model
        Resident residentSaved = residentRepository.save(resident);
        return ResponseEntity.ok(residentSaved);
    }

}
