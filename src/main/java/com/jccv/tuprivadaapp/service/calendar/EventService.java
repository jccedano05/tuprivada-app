package com.jccv.tuprivadaapp.service.calendar;

import com.jccv.tuprivadaapp.dto.calendar.EventDto;

import java.util.List;
import java.util.Map;

public interface EventService {
    EventDto createEvent(EventDto eventDto);
    EventDto updateEvent(Long id, EventDto eventDto);
    boolean deleteEvent(Long id);
    Map<String, List<EventDto>> getEventsByCondominiumAndMonth(Long condominiumId, int month, int year);
}
