package com.jccv.tuprivadaapp.controller.contact;

import com.jccv.tuprivadaapp.dto.contact.ContactDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.service.contact.ContactService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;


    private final ResidentService residentService;

    @Autowired
    public ContactController(ContactService contactService, ResidentService residentService) {
        this.contactService = contactService;
        this.residentService = residentService;
    }

    @GetMapping
    public List<Contact> getAllContacts() {
        return contactService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContactById(@PathVariable Long id) {
       try{
           return ResponseEntity.ok(contactService.findById(id));
       } catch (ResourceNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }catch (BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }}

    @GetMapping("/residents/{residentId}")
    public List<Contact> getContactsByResidentId(@PathVariable Long residentId) {
        return contactService.findByResidentId(residentId);
    }

    @GetMapping("/condominiums/{condominiumId}")
    public List<Contact> getContactsByCondominiumId(@PathVariable Long condominiumId) {
        return contactService.findByCondominiumId(condominiumId);
    }

    @PostMapping("/residents")
    public ResponseEntity<?> createContactResident(@RequestBody ContactDto contactDTO) {

        try {
            Optional<Resident> resident = residentService.getResidentById(contactDTO.getResidentId());
            if (!resident.isPresent()) {
                throw new ResourceNotFoundException("Resident not found");
            }
            contactDTO.setCondominiumId(null);
            return new ResponseEntity<>(contactService.save(contactDTO), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/condominiums")
    public ResponseEntity<?> createContactCondominium(@RequestBody ContactDto contactDTO) {
        try{contactDTO.setResidentId(null);
            return new ResponseEntity<>(contactService.save(contactDTO), HttpStatus.CREATED);
    }catch (ResourceNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }}

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable Long id, @RequestBody ContactDto contactDTO) {
        try{
            if(contactDTO.getCondominiumId() != null && contactDTO.getResidentId() != null){
                new BadRequestException("condominiumId and residentId must not be contained at the same time");
            }
            ContactDto updatedContact = contactService.update(id, contactDTO);
            return new ResponseEntity<>(updatedContact, HttpStatus.OK);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
}