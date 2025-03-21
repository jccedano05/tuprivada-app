package com.jccv.tuprivadaapp.service.contact.implementation;
import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.contact.ContactDto;
import com.jccv.tuprivadaapp.dto.contact.mapper.ContactMapper;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.contact.ContactRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.contact.ContactService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
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
    private final PollingNotificationService pollingNotificationService;
    private final OneSignalPushNotificationService oneSignalPushNotificationService;

    @Autowired
    public ContactServiceImp(ContactRepository contactRepository, ContactMapper contactMapper, CondominiumService condominiumService, PollingNotificationService pollingNotificationService, OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
        this.condominiumService = condominiumService;
        this.pollingNotificationService = pollingNotificationService;
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
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

        if(contact.getResident() != null && contact.getResident().getUser() != null && contact.getResident().getUser().getRole().equals(Role.ADMIN)){
            pollingNotificationService.createNotificationForCondominium(contact.getCondominium().getId(), PollingNotificationDto.builder()
                    .title("Contacto '" + contact.getName() + "' agregado a tu comunidad")
                    .message("Ahora el numero telefonico de este contacto esta disponible para verlo en la seccion de Contactos")
                    .read(false)
                    .build());

            oneSignalPushNotificationService.sendPushToCondominium(contact.getCondominium().getId(), PushNotificationRequest.builder()
                    .title("Nuevo contacto en tu comunidad")
                    .message("Ahora puedes ver a '" + contact.getName() + "' en la seccion de Contactos")
                    .build());
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
        Contact contact = findById(id);
        contactRepository.delete(contact);
    }
}