package com.jccv.tuprivadaapp.controller.resident;

import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.resident.AddressResident;
import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import com.jccv.tuprivadaapp.repository.resident.dto.PaymentResidentDto;
import com.jccv.tuprivadaapp.service.resident.AddressResidentService;
import com.jccv.tuprivadaapp.service.resident.PaymentResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.*;

@RestController
@RequestMapping("addressResidents")
@PreAuthorize(USER_LEVEL)
public class AddressResidentController {


    @Autowired
    private AddressResidentService addressResidentService;

    @GetMapping("{id}")
    public ResponseEntity<?> getAddressResidentById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(addressResidentService.getAddressResidentById(id), HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("condominiums/{condominiumId}")

    @PreAuthorize(CONDOMINIUM_LEVEL)
    public ResponseEntity<?> getAllAddressResidentByCondominiumId(@PathVariable Long condominiumId) {
        try {
            return new ResponseEntity<>(addressResidentService.getAllAddressResidentsByCondominiumId(condominiumId), HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @PreAuthorize(CONDOMINIUM_LEVEL)
    public ResponseEntity<?> createAddressResident(@RequestBody AddressResidentDto addressResident) {
        try {
            return new ResponseEntity<>(addressResidentService.createAddressResident(addressResident), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("residents/{id}")
    public ResponseEntity<?> getAddressResidentByResidentId(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(addressResidentService.getAddressResidentByResidentId(id), HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
