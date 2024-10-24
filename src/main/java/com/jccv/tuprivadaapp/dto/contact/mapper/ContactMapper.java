package com.jccv.tuprivadaapp.dto.contact.mapper;

import com.jccv.tuprivadaapp.dto.contact.ContactDto;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    private final CondominiumService condominiumService;

    private final ResidentService residentService;

    @Autowired
    public ContactMapper(CondominiumService condominiumService, ResidentService residentService) {
        this.condominiumService = condominiumService;
        this.residentService = residentService;
    }

    public Contact toEntity(ContactDto contactDto) {
        Contact contact = Contact.builder()
                .id(contactDto.getId())
                .name(contactDto.getName())
                .phone(contactDto.getPhone())
                .description(contactDto.getDescription())
                .build();
        if(contactDto.getCondominiumId() != null){
            contact.setCondominium(condominiumService.findById(contactDto.getCondominiumId()));
        }
        if(contactDto.getResidentId() != null){
            contact.setResident(residentService.getResidentById(contactDto.getResidentId()).get());
        }
        return contact;
    }

    public ContactDto toDTO(Contact contact) {
        ContactDto dto = ContactDto.builder()
                .id(contact.getId())
                .name(contact.getName())
                .phone(contact.getPhone())
                .description(contact.getDescription())
                .build();

        if(contact.getCondominium() != null){
            dto.setCondominiumId(contact.getCondominium().getId());
        }
        if(contact.getResident() != null){
            dto.setResidentId(contact.getResident().getId());
        }
        return dto;
    }
}