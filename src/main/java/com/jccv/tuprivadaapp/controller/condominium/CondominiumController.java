package com.jccv.tuprivadaapp.controller.condominium;

import com.jccv.tuprivadaapp.dto.condominium.CondominiumDto;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.MAX_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.USER_LEVEL;

@RestController
@RequestMapping("condominiums")
@PreAuthorize(MAX_LEVEL)
public class CondominiumController {

    @Autowired
    private  CondominiumService condominiumService;




    @PostMapping
    public ResponseEntity<?> createCondominium(@RequestBody CondominiumDto condominium) {
        try {
            Condominium createdCondominium = condominiumService.create(condominium);
            return new ResponseEntity<>(createdCondominium, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCondominiums() {
        try {
            List<Condominium> condominiums = condominiumService.findAll();
            return new ResponseEntity<>(condominiums, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize(USER_LEVEL)
    public ResponseEntity<?> getCondominiumById(@PathVariable Long id) {
        try {
            Condominium condominium = condominiumService.findById(id);
            return new ResponseEntity<>(condominium, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCondominium(@PathVariable Long id, @RequestBody CondominiumDto condominium) {
        try {
            condominium.setId(id); // Asegúrate de establecer el ID del condominio
            Condominium updatedCondominium = condominiumService.update(condominium);
            return new ResponseEntity<>(updatedCondominium, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCondominium(@PathVariable Long id) {
        try {
            condominiumService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}