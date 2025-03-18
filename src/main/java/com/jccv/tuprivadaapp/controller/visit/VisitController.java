package com.jccv.tuprivadaapp.controller.visit;



import com.jccv.tuprivadaapp.dto.visit.VisitDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    @Autowired
    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createVisit(@RequestBody VisitDto visitDto) {
        try {
            VisitDto createdVisit = visitService.createVisit(visitDto);
            return new ResponseEntity<>(createdVisit, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateQr(@RequestParam("token") String qrToken) {
        try {
            VisitDto visitDto = visitService.validateQrToken(qrToken);
            return new ResponseEntity<>(visitDto, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para obtener visitas activas de un residente
    @GetMapping("/active")
    public ResponseEntity<?> getActiveVisits(@RequestParam("userId") Long userId) {
        try {
            List<VisitDto> activeVisits = visitService.getActiveVisits(userId);
            return new ResponseEntity<>(activeVisits, HttpStatus.OK);
        }  catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para obtener el historial anual de visitas de un residente
    @GetMapping("/history")
    public ResponseEntity<?> getAnnualHistory(
            @RequestParam("userId") Long userId,
            @RequestParam("year") int year) {
        try {
            List<VisitDto> history = visitService.getAnnualHistory(userId, year);
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<?> getDailyVisits(
            @RequestParam("condominiumId") Long condominiumId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);
            List<VisitDto> dailyVisits = visitService.getDailyVisits(condominiumId, start, end);
            return new ResponseEntity<>(dailyVisits, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

