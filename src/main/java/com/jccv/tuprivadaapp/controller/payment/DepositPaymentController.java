package com.jccv.tuprivadaapp.controller.payment;

import com.jccv.tuprivadaapp.dto.payment.DepositPaymentDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.payment.DepositPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit-payments")
public class DepositPaymentController {

    @Autowired
    private DepositPaymentService depositPaymentService;

    // Endpoint para crear un abono para un Payment específico
    @PostMapping("/{paymentId}")
    public ResponseEntity<?> addDeposit(@PathVariable Long paymentId, @RequestBody DepositPaymentDto depositPaymentDTO) {
        try {
            DepositPaymentDto createdDeposit = depositPaymentService.addDeposit(paymentId, depositPaymentDTO);
            return new ResponseEntity<>(createdDeposit, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para listar los abonos asociados a un Payment
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getDeposits(@PathVariable Long paymentId) {
        try {
            List<DepositPaymentDto> deposits = depositPaymentService.getDepositsByPayment(paymentId);
            return new ResponseEntity<>(deposits, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{depositId}")
    public ResponseEntity<?> updateDeposit(@PathVariable Long depositId, @RequestBody DepositPaymentDto updatedDto) {
        try {
            DepositPaymentDto updatedDeposit = depositPaymentService.updateDeposit(depositId, updatedDto);
            return new ResponseEntity<>(updatedDeposit, HttpStatus.OK);
        } catch (ResourceNotFoundException | BadRequestException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{depositId}")
    public ResponseEntity<?> deleteDeposit(@PathVariable Long depositId) {
        try {
            depositPaymentService.deleteDeposit(depositId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
