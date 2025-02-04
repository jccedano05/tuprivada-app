package com.jccv.tuprivadaapp.service.calendar.implementation;

import com.jccv.tuprivadaapp.dto.calendar.EventDto;
import com.jccv.tuprivadaapp.dto.calendar.mapper.EventMapper;
import com.jccv.tuprivadaapp.model.calendar.Event;
import com.jccv.tuprivadaapp.model.calendar.UserEvent;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.calendar.EventRepository;
import com.jccv.tuprivadaapp.repository.calendar.UserEventRepository;
import com.jccv.tuprivadaapp.service.calendar.EventService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImp implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CondominiumService condominiumService;
    private final UserEventRepository userEventRepository;

    @Override
    public EventDto createEvent(EventDto eventDto) {

        Condominium condominium = condominiumService.findById(eventDto.getCondominiumId());

        // Mapear el DTO a la entidad Event y asignar el condominio
        Event event = eventMapper.eventDtoToEvent(eventDto, condominium);

        event = eventRepository.save(event);
        return eventMapper.eventToEventDto(event);
    }

    @Override
    public EventDto updateEvent(Long id, EventDto eventDto) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());
        event.setDate(eventDto.getDate());
        event.setStartTime(eventDto.getStartTime());
        event.setEndTime(eventDto.getEndTime());
        event = eventRepository.save(event);
        return eventMapper.eventToEventDto(event);
    }

    @Override
    public boolean deleteEvent(Long id) {
        eventRepository.deleteById(id);
        return !eventRepository.existsById(id);
    }

    @Override
    public Map<String, List<EventDto>> getEventsByCondominiumAndMonth(Long condominiumId, int month, int year) {
        List<Event> events = eventRepository.findByCondominiumIdAndMonth(condominiumId, month, year);

        // Mapa para organizar los eventos por fecha en el formato "yyyy-MM-dd"
        Map<String, List<EventDto>> eventsByDate = new HashMap<>();

        for (Event event : events) {
            String eventDate = event.getDate().toString();  // Obtener la fecha en formato 'yyyy-MM-dd'

            // Si no existe una lista de eventos para esa fecha, la creamos
            eventsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>())
                    .add(eventMapper.eventToEventDto(event));
        }

        return eventsByDate;
    }

    @Override
    public List<EventDto> getNextEvent(Long condominiumId) {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findNextEventByCondominiumId(condominiumId, today);

        // Agrupamos los eventos por fecha
        Map<LocalDate, List<Event>> eventsByDate = events.stream()
                .collect(Collectors.groupingBy(Event::getDate));

        // Obtenemos el primer día con eventos
        Optional<LocalDate> firstEventDay = eventsByDate.keySet().stream().min(Comparator.naturalOrder());

        // Si existe ese día, devolvemos los eventos de ese día, de lo contrario una lista vacía
        return firstEventDay
                .map(date -> eventsByDate.get(date).stream()
                        .map(eventMapper::eventToEventDto)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
