package com.jccv.tuprivadaapp.service.calendar.implementation;

import com.jccv.tuprivadaapp.dto.calendar.UserEventDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.calendar.Event;
import com.jccv.tuprivadaapp.model.calendar.UserEvent;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.repository.calendar.EventRepository;
import com.jccv.tuprivadaapp.repository.calendar.UserEventRepository;
import com.jccv.tuprivadaapp.service.calendar.UserEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEventServiceImp implements UserEventService {

    @Autowired
    private final UserEventRepository userEventRepository;

    @Autowired
    private final EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean addEventToCalendar(UserEventDto userEventDto) {
        Optional<UserEvent> existingUserEvent = userEventRepository.findByEventIdAndDeviceId(userEventDto.getEventId(), userEventDto.getDeviceId());
        if (existingUserEvent.isPresent()) {
            return false;  // Ya ha sido agregado
        }

        if(userEventDto.getDeviceId() == null || userEventDto.getDeviceId().trim().isEmpty()){
            throw new BadRequestException("DeviceId incorrect");
        }

        User user = userRepository.findById(userEventDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(userEventDto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));


        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);  // Suponiendo que tienes el método getUserId
        userEvent.setEvent(event);  // Suponiendo que tienes el método getEventId
        userEvent.setDeviceId(userEventDto.getDeviceId());
        userEventRepository.save(userEvent);
        return true;
    }
}
