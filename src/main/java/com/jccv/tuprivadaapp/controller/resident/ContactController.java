package com.jccv.tuprivadaapp.controller.resident;

import com.jccv.tuprivadaapp.model.resident.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.service.resident.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController()
@RequestMapping("residents/{idResident}/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;


    @GetMapping
    public ResponseEntity<List<Contact>> getAllContactsResident(@PathVariable Long idResident){
        List<Contact> contacts = contactService.getAllContactsByResidentId(idResident).orElse(null);
        return ResponseEntity.ok(contacts);
    }

    @PostMapping()
    public ResponseEntity<Contact> createContact(@PathVariable Long idResident ,@RequestBody Contact contact){
        Contact contactSaved = contactService.saveContact(idResident, contact);
        return ResponseEntity.ok(contactSaved);
    }

}
