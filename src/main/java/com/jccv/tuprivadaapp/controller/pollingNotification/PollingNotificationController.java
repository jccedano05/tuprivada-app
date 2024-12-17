package com.jccv.tuprivadaapp.controller.pollingNotification;

import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;

@RestController
@RequestMapping("/api/polling-notifications")
public class PollingNotificationController {

        @Autowired
        private final PollingNotificationService pollingNotificationService;

    public PollingNotificationController(PollingNotificationService pollingNotificationService) {
        this.pollingNotificationService = pollingNotificationService;
    }

    @PostMapping
    @PreAuthorize(CONDOMINIUM_LEVEL)
    public ResponseEntity<?> createPollingNotificationForUser(@RequestBody PollingNotificationDto pollingNotificationDto) {
        try{
            pollingNotificationService.createNotification(pollingNotificationDto);
            return ResponseEntity.ok().build();

        } catch (
                ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (
                BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }}

    @PostMapping("/condominiums/{condominiumId}")
    @PreAuthorize(CONDOMINIUM_LEVEL)
    public ResponseEntity<?> createPollingNotificationForCondominiumResidents(@PathVariable Long condominiumId, @RequestBody PollingNotificationDto pollingNotificationDto) {
        try{
            pollingNotificationService.createNotificationForCondominium(condominiumId, pollingNotificationDto);
            return ResponseEntity.ok().build();

        } catch (
                ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (
                BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }}


    @PostMapping("/mark-as-read")
    public ResponseEntity<?> markNotificationsAsRead(@RequestBody List<Long> notificationIds) {
       try{
           pollingNotificationService.markNotificationsAsRead(notificationIds);
           return ResponseEntity.ok().build();

    } catch (
    ResourceNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }catch (
    BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }}

    @GetMapping("/unread/{userId}")
        public ResponseEntity<?> getUnreadNotifications(@PathVariable Long userId) {
        try{
            return new ResponseEntity<>(pollingNotificationService.getUnreadNotificationsForUser(userId), HttpStatus.OK);
        } catch (
    ResourceNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }catch (
    BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }}

}


