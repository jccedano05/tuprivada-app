package com.jccv.tuprivadaapp.controller.resident;

import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.repository.resident.dto.PaymentResidentDto;
import com.jccv.tuprivadaapp.service.resident.PaymentResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("paymentResident")
public class PaymentResidentController {

    @Autowired
    private PaymentResidentService paymentResidentService;


    @GetMapping("{id}")
    public ResponseEntity<?> getPaymentResidentById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(paymentResidentService.getPaymentResidentById(id), HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("residents/{residentId}")
    public ResponseEntity<?> getAllPaymentsByResidentId(@PathVariable Long residentId) {
        try {
            return new ResponseEntity<>(paymentResidentService.getAllPaymentsByResidentId(residentId), HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("condominiums/{condominiumId}")
    public ResponseEntity<?> getAllPaymentsByCondominiumId(@PathVariable Long condominiumId) {
        try {
            return new ResponseEntity<>(paymentResidentService.getAllPaymentsByCondominiumId(condominiumId), HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> createPaymentResident(@RequestBody PaymentResidentDto paymentResidentDto) {
        try {
            return new ResponseEntity<>(paymentResidentService.savePaymentResident(paymentResidentDto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
