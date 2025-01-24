package com.jccv.tuprivadaapp.controller.transaction;

import com.jccv.tuprivadaapp.dto.transaction.DepositDto;
import com.jccv.tuprivadaapp.dto.transaction.DepositSummaryDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.transaction.Deposit;
import com.jccv.tuprivadaapp.service.transaction.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.RESIDENT_LEVEL;

@RestController
@RequestMapping("/deposits")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class DepositController {

    private final DepositService depositService;

    @Autowired
    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }


    @PostMapping
    public ResponseEntity<?> createDeposit(@RequestBody DepositDto depositDTO) {
        try{
            DepositDto depositDto = depositService.createDeposit(depositDTO);
            return new ResponseEntity<>(depositDto, HttpStatus.CREATED);

    }
       catch (
    ResourceNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
       catch (
    BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
       catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }}


    // Actualizar depósito
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeposit(@PathVariable Long id, @RequestBody DepositDto depositDTO) {
        try {
            DepositDto depositDto = depositService.updateDeposit(id, depositDTO);
            return new ResponseEntity<>(depositDto, HttpStatus.OK);
        }
        catch (
                ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (
                BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }}


    // Eliminar depósito
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeposit(@PathVariable Long id) {
        try {
            if (depositService.deleteDeposit(id)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        catch (
                ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (
                BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }}




    // Buscar todos los depósitos por residentId
    @GetMapping("/residents/{residentId}")
    @PreAuthorize(RESIDENT_LEVEL)
    public ResponseEntity<?> findDepositsByResidentId(@PathVariable Long residentId) {
       try{
           List<DepositDto> deposits = depositService.findDepositsByResidentId(residentId);
           return new ResponseEntity<>(deposits, HttpStatus.OK);
    }
       catch (
ResourceNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (
BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }}

    // Obtener depósitos por mes y año para un condominio
    @GetMapping("/condominiums/{condominiumId}/month")
    public ResponseEntity<?> getDepositsByCondominiumIdAndMonth(@PathVariable Long condominiumId,
                                                                @RequestParam int month,
                                                                @RequestParam int year) {
        try {
            List<DepositDto> deposits = depositService.getDepositsByCondominiumIdAndMonth(condominiumId, month, year);
            return new ResponseEntity<>(deposits, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener depósitos por año para un condominio
    @GetMapping("/condominiums/{condominiumId}/year")
    public ResponseEntity<?> getDepositsByCondominiumIdAndYear(@PathVariable Long condominiumId,
                                                               @RequestParam int year) {
        try {
            List<DepositDto> deposits = depositService.getDepositsByCondominiumIdAndYear(condominiumId, year);
            return new ResponseEntity<>(deposits, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<DepositSummaryDto> getDepositSummary(
            @RequestParam Long condominiumId,
            @RequestParam int month,
            @RequestParam int year) {
        DepositSummaryDto summary = depositService.getDepositSummaryForMonthAndYear(condominiumId, month, year);
        return ResponseEntity.ok(summary);
    }

}
