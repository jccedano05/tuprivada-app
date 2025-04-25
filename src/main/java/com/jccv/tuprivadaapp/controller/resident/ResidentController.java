package com.jccv.tuprivadaapp.controller.resident;

import com.jccv.tuprivadaapp.dto.resident.ResidentDto;
import com.jccv.tuprivadaapp.dto.resident.ResidentRelevantInfoDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("residents")
public class ResidentController {

    private final ResidentRepository residentRepository;
    private final ResidentService residentService;

    @Autowired
    public ResidentController(ResidentRepository residentRepository, ResidentService residentService) {
        this.residentRepository = residentRepository;
        this.residentService = residentService;
    }

    @PostMapping
    public ResponseEntity<Resident> createResident(@RequestBody Resident resident){
        //Hacer un DTO y guardar como Model
        Resident residentSaved = residentRepository.save(resident);
        return ResponseEntity.ok(residentSaved);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getResidentById(@PathVariable Long id) {
        try{
            return new ResponseEntity<>(residentService.getResidentById(id), HttpStatus.OK);
        }catch (
                ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dto/{id}")
    public ResponseEntity<?> getResidentDtoById(@PathVariable Long id) {
        try {
            ResidentRelevantInfoDto dto = residentService.getResidentDtoById(id);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("balance/{id}")
    public ResponseEntity<?> getBalanceOfResidentById(@PathVariable Long id) {
        try{
            return new ResponseEntity<>(residentService.getBalanceOfResidentById(id), HttpStatus.OK);
        }catch (
                ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("condominiums/{condominiumId}")
    public ResponseEntity<?> getAllResidentsWithCondominiumId(@PathVariable Long condominiumId) {
        try{
            return new ResponseEntity<>(residentService.getAllResidentsWithCondominiumId(condominiumId), HttpStatus.OK);
        }catch (
                ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getResidentByUserId(@PathVariable Long userId) {
        try{
         residentService.getResidentByUserId(userId);
            return new ResponseEntity<>(residentService.getResidentByUserId(userId), HttpStatus.OK);
    }catch (
    ResourceNotFoundException e) {
            e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
        catch (Exception e) {
            e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }

}
