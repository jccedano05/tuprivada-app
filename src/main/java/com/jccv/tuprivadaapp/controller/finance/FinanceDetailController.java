package com.jccv.tuprivadaapp.controller.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceDetailDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.finance.FinanceDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.RESIDENT_LEVEL;

@RestController
@RequestMapping("/finance-details")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class FinanceDetailController {

    private final FinanceDetailService financeDetailService;

    @Autowired
    public FinanceDetailController(FinanceDetailService financeDetailService) {
        this.financeDetailService = financeDetailService;
    }

    @PostMapping
    public ResponseEntity<?> createFinanceDetail(@RequestBody FinanceDetailDto financeDetailDto) {
        try {
            FinanceDetailDto createdFinanceDetail = financeDetailService.createFinanceDetail(financeDetailDto);
            return new ResponseEntity<>(createdFinanceDetail, HttpStatus.CREATED);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFinanceDetailById(@PathVariable Long id) {
        try {
            FinanceDetailDto financeDetailDto = financeDetailService.getFinanceDetailById(id);
            return new ResponseEntity<>(financeDetailDto, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize(RESIDENT_LEVEL)
    @GetMapping("/finance/{financeId}")
    public ResponseEntity<?> getFinanceDetailsByFinance(@PathVariable Long financeId) {
        try {
            List<FinanceDetailDto> financeDetails = financeDetailService.getFinanceDetailsByFinance(financeId);
            return new ResponseEntity<>(financeDetails, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFinanceDetail(@PathVariable Long id, @RequestBody FinanceDetailDto financeDetailDto) {
        try {
            FinanceDetailDto updatedFinanceDetail = financeDetailService.updateFinanceDetail(id, financeDetailDto);
            return new ResponseEntity<>(updatedFinanceDetail, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFinanceDetail(@PathVariable Long id) {
        try {
            financeDetailService.deleteFinanceDetail(id);
            return new ResponseEntity<>("Detalle de finanza eliminado correctamente", HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
