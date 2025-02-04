package com.jccv.tuprivadaapp.controller.event;

import com.jccv.tuprivadaapp.dto.calendar.UserEventDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.calendar.UserEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-events")
@RequiredArgsConstructor
public class UserEventController {

    private final UserEventService userEventService;

    @PostMapping("/add")
    public ResponseEntity<?> addEventToUserCalendar(
            @RequestBody UserEventDto userEventDto) {
       try{
           boolean isAdded = userEventService.addEventToCalendar(userEventDto);
           if (isAdded) {
               return new ResponseEntity<>(HttpStatus.CREATED);
           } else {
               return new ResponseEntity<>("Event already added", HttpStatus.CONFLICT);
           }
       } catch (ResourceNotFoundException e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
       } catch (BadRequestException e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
       } catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }
}
