package com.jccv.tuprivadaapp.controller.condominium;

import com.jccv.tuprivadaapp.dto.condominium.AddressDto;
import com.jccv.tuprivadaapp.model.condominium.Address;
import com.jccv.tuprivadaapp.service.condominium.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.MAX_LEVEL;

@RestController
@RequestMapping("addresses")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class AddressController {

    @Autowired
    private AddressService addressService;


    @PostMapping
    @PreAuthorize(MAX_LEVEL)
    public ResponseEntity<?> createAddress(@RequestBody AddressDto address) {
        try{
        Address createdAddress = addressService.create(address);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAddresses() {
        try{
        List<Address> addresses = addressService.findAll();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable Long id) {
        try{
        Address address = addressService.findById(id);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }
        catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody AddressDto address) {
        try{
        address.setId(id); // Asegúrate de establecer el ID del objeto Address
        Address updatedAddress = addressService.update(address);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }
        catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MAX_LEVEL)
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        try{
        addressService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
        catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    }
}
