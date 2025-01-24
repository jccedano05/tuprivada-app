package com.jccv.tuprivadaapp.dto.calendar.mapper;

import com.jccv.tuprivadaapp.model.calendar.Event;
import com.jccv.tuprivadaapp.dto.calendar.EventDto;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    // Método para convertir de Event a EventDto
    public EventDto eventToEventDto(Event event) {
        if (event == null) {
            return null;
        }
        EventDto eventDto = new EventDto();
        eventDto.setId(event.getId());
        eventDto.setTitle(event.getTitle());
        eventDto.setDescription(event.getDescription());
        eventDto.setDate(event.getDate());
        eventDto.setStartTime(event.getStartTime());
        eventDto.setEndTime(event.getEndTime());
        eventDto.setCondominiumId(event.getCondominium().getId());

        return eventDto;
    }

    // Método para convertir de EventDto a Event
    public Event eventDtoToEvent(EventDto eventDto, Condominium condominium) {
        if (eventDto == null) {
            return null;
        }
        Event event = new Event();
        event.setId(eventDto.getId());
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());
        event.setDate(eventDto.getDate());
        event.setStartTime(eventDto.getStartTime());
        event.setEndTime(eventDto.getEndTime());
        event.setCondominium(condominium);
        // Si tu entidad Event tiene un objeto Condominium, aquí también deberías establecerlo si se proporciona.
        return event;
    }
}
