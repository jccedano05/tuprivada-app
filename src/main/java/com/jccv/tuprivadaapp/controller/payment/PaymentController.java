package com.jccv.tuprivadaapp.controller.payment;


import com.jccv.tuprivadaapp.dto.payment.PaymentDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentSummaryDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.*;

@RestController
@RequestMapping("payments")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody PaymentDto paymentDto) {
        try{
            return new ResponseEntity<>(paymentService.create(paymentDto), HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAddresses() {
        try{
            return new ResponseEntity<>(paymentService.findAll(), HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable Long id) {
        try{
            return new ResponseEntity<>(paymentService.findById(id), HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody PaymentDto paymentDto) {
        try{
            paymentDto.setId(id);
            return new ResponseEntity<>(paymentService.update(paymentDto), HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize(USER_LEVEL)
    @GetMapping("/resident/{residentId}/unpaid")
    public ResponseEntity<Page<Payment>> getUnpaidPayments(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page,   // Página por defecto es 0
            @RequestParam(defaultValue = "10") int size) {  // Tamaño por defecto es 10
        // Llamada al servicio con paginación
        Page<Payment> unpaidPayments = paymentService.getUnpaidPaymentsForResident(residentId, page, size);

        // Retornar la respuesta con el objeto Page
        return ResponseEntity.ok(unpaidPayments);
    }

    @PreAuthorize(USER_LEVEL)
    @GetMapping("/resident/{residentId}/paid")
    public ResponseEntity<Page<Payment>> getPaidPayments(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Payment> paidPayments = paymentService.getPaidPaymentsForResident(residentId, page, size);
        return ResponseEntity.ok(paidPayments);
    }

    @PreAuthorize(USER_LEVEL)
    @GetMapping("/resident/{residentId}/payments")
    public ResponseEntity<Page<Payment>> getPaymentsInRange(
            @PathVariable Long residentId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Payment> paymentsInRange = paymentService.getPaymentsInRangeForResident(residentId, startDate, endDate, page, size);
        return ResponseEntity.ok(paymentsInRange);
    }

    // Endpoint para obtener el total de la deuda
    // Endpoint para obtener el total de la deuda
    @PreAuthorize(USER_LEVEL)
    @GetMapping("/resident/{residentId}/total-debt")
    public ResponseEntity<Double> getTotalDebt(@PathVariable Long residentId) {
        return ResponseEntity.ok(paymentService.getTotalDebtForResident(residentId));
    }

    @PreAuthorize(USER_LEVEL)
    // Endpoint para obtener la próxima fecha de vencimiento
    @GetMapping("/resident/{residentId}/next-due-date")
    public ResponseEntity<LocalDateTime> getNextDueDate(@PathVariable Long residentId) {
        return ResponseEntity.ok(paymentService.getRelevantDueDateForResident(residentId));
    }

    @PreAuthorize(USER_LEVEL)
    @GetMapping("/resident/{residentId}/summary")
    public ResponseEntity<?> getPaymentSummary(@PathVariable Long residentId) {
        try{
            return ResponseEntity.ok(paymentService.getPaymentSummaryForResident(residentId));
        }catch (ResourceNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



}
