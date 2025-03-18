package com.jccv.tuprivadaapp.controller.reservation;


import com.jccv.tuprivadaapp.dto.reservation.ReservationDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Crear una nueva reserva
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationDto dto) {
        try {
            ReservationDto created = reservationService.createReservation(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar una reserva existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody ReservationDto dto) {
        try {
            ReservationDto updated = reservationService.updateReservation(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cancelar (o eliminar) una reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            boolean canceled = reservationService.cancelReservation(id);
            return new ResponseEntity<>(canceled, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener reservas de un residente para un día específico
    @GetMapping("/resident/{residentId}/date/{date}")
    public ResponseEntity<?> getReservationsByResidentAndDate(
            @PathVariable Long residentId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ReservationDto> list = reservationService.getReservationsByResidentAndDate(residentId, date);
            return new ResponseEntity<>(list, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener reservas de un condominio para un día específico
    @GetMapping("/condominium/{condominiumId}/date/{date}")
    public ResponseEntity<?> getReservationsByCondominiumAndDate(
            @PathVariable Long condominiumId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ReservationDto> list = reservationService.getReservationsByCondominiumAndDate(condominiumId, date);
            return new ResponseEntity<>(list, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para obtener reservas activas de un residente
    @GetMapping("/resident/{residentId}/active")
    public ResponseEntity<?> getActiveReservationsByResident(@PathVariable Long residentId) {
        try {
            List<ReservationDto> activeReservations = reservationService.getActiveReservationsByResident(residentId);
            return new ResponseEntity<>(activeReservations, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para obtener historial de reservas por año de un residente
    @GetMapping("/resident/{residentId}/history/{year}")
    public ResponseEntity<?> getReservationHistoryByResidentAndYear(@PathVariable Long residentId, @PathVariable int year) {
        try {
            List<ReservationDto> historyReservations = reservationService.getReservationHistoryByResidentAndYear(residentId, year);
            return new ResponseEntity<>(historyReservations, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener reservas activas de un condominio por mes
    @GetMapping("/condominium/{condominiumId}/active/{year}/{month}")
    public ResponseEntity<?> getActiveReservationsByCondominiumAndMonth(
            @PathVariable Long condominiumId,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            List<ReservationDto> activeReservations = reservationService.getActiveReservationsByCondominiumAndMonth(condominiumId, year, month);
            return new ResponseEntity<>(activeReservations, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener historial de reservas de un condominio por mes (solo completadas o canceladas antes de la fecha actual)
    @GetMapping("/condominium/{condominiumId}/history/{year}/{month}")
    public ResponseEntity<?> getReservationHistoryByCondominiumAndMonth(
            @PathVariable Long condominiumId,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            List<ReservationDto> historyReservations = reservationService.getReservationHistoryByCondominiumAndMonth(condominiumId, year, month);
            return new ResponseEntity<>(historyReservations, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
