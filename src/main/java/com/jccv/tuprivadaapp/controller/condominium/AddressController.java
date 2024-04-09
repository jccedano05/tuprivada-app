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
@RequestMapping("address")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class AddressController {

    @Autowired
    private AddressService addressService;


    @PostMapping
    @PreAuthorize(MAX_LEVEL)
    public ResponseEntity<Address> createAddress(@RequestBody AddressDto address) {
        Address createdAddress = addressService.create( address);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.findAll();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        Address address = addressService.findById(id);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody AddressDto address) {
        address.setId(id); // Asegúrate de establecer el ID del objeto Address
        Address updatedAddress = addressService.update(address);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MAX_LEVEL)
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
