package com.jccv.tuprivadaapp.service.calendar;

import com.jccv.tuprivadaapp.dto.calendar.UserEventDto;

public interface UserEventService {
    boolean addEventToCalendar(UserEventDto userEventDto);
}