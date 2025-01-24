package com.jccv.tuprivadaapp.controller.event;

import com.jccv.tuprivadaapp.dto.calendar.EventDto;
import com.jccv.tuprivadaapp.service.calendar.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Crear evento
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventDto eventDto) {
        try {
            EventDto createdEvent = eventService.createEvent(eventDto);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar evento
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        try {
            EventDto updatedEvent = eventService.updateEvent(id, eventDto);
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar evento
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            if (eventService.deleteEvent(id)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener eventos por condominio y mes
    @GetMapping("/condominiums/{condominiumId}/month/{month}/year/{year}")
    public ResponseEntity<?> getEventsByMonth(
            @PathVariable Long condominiumId,
            @PathVariable int month,
            @PathVariable int year) {
        try {
            return new ResponseEntity<>(eventService.getEventsByCondominiumAndMonth(condominiumId, month, year), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
