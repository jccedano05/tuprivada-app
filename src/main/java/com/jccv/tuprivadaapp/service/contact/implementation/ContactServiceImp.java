package com.jccv.tuprivadaapp.service.contact.implementation;
import com.jccv.tuprivadaapp.dto.contact.ContactDto;
import com.jccv.tuprivadaapp.dto.contact.mapper.ContactMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.contact.ContactRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.contact.ContactService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImp implements ContactService {
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final CondominiumService condominiumService;

    @Autowired
    public ContactServiceImp(ContactRepository contactRepository, ContactMapper contactMapper, CondominiumService condominiumService) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
        this.condominiumService = condominiumService;
    }

    @Override
    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    @Override
    public Contact findById(Long id) {
        return contactRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
    }

    @Override
    public List<Contact> findByResidentId(Long residentId) {
        return contactRepository.findByResidentId(residentId);
    }

    @Override
    public List<Contact> findByCondominiumId(Long condominiumId) {
        return contactRepository.findByCondominiumId(condominiumId);
    }


    @Override
    public ContactDto save(ContactDto contactDTO) {
        // Verificar si el residente existe

        if (contactDTO.getResidentId() != null && contactDTO.getCondominiumId() != null) {
            throw new BadRequestException("A contact can belong only to a resident or a condominium, not both.");
        }
        if (contactDTO.getResidentId() == null && contactDTO.getCondominiumId() == null) {
            throw new BadRequestException("A contact can belong must contain a residentId or a condominiumId.");
        }


        // Crear la entidad Contact a partir del DTO
        Contact contact = contactMapper.toEntity(contactDTO);

        // Verificar si el residente existe solo si se proporciona residentId


        // Verificar si el condominio existe
        if( contactDTO.getCondominiumId() != null){
            Condominium condominium = condominiumService.findById(contactDTO.getCondominiumId());
            contact.setCondominium(condominium);
        }


        return contactMapper.toDTO(contactRepository.save(contact));
    }

    @Override
    public ContactDto update(Long id, ContactDto contactDTO) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        contact.setName(contactDTO.getName());
        contact.setPhone(contactDTO.getPhone());
        contact.setDescription(contactDTO.getDescription());

        return contactMapper.toDTO(contactRepository.save(contact));
    }

    @Override
    public void deleteById(Long id) {
        contactRepository.deleteById(id);
    }
}