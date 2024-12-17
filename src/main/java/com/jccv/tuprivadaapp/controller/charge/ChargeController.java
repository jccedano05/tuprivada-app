package com.jccv.tuprivadaapp.controller.charge;

import com.jccv.tuprivadaapp.dto.charge.ChargeDto;
import com.jccv.tuprivadaapp.dto.charge.ChargeSummaryDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentResidentDetailsDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.service.charge.ChargeService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/charges")
public class ChargeController {

    private final ResidentService residentService;
    private final ChargeService chargeService;
    private final PaymentService paymentService;

    @Autowired
    public ChargeController(ResidentService residentService, ChargeService chargeService, PaymentService paymentService) {
        this.residentService = residentService;
        this.chargeService = chargeService;
        this.paymentService = paymentService;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyCharge(@RequestBody ChargeDto chargeRequestDto) {
        System.out.println("Entro al apply");
        try {
            if (chargeRequestDto.getResidentIds() == null || chargeRequestDto.getResidentIds().isEmpty()) {
                return ResponseEntity.badRequest().body("Debe proporcionar al menos un residente.");
            }

            List<Resident> residents = residentService.getResidentsByIds(chargeRequestDto.getResidentIds());
            Charge charge = null;
            if(chargeRequestDto.getId() != null){
                charge = chargeService.findById(chargeRequestDto.getId());
            }else{
                System.out.println();
                charge = chargeService.createCharge(chargeRequestDto);
            }
            System.out.println(charge.toString());
            List<Payment> payments = paymentService.applyChargeToResidents(residents, charge);

            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{chargeId}")
    public ResponseEntity<?> updateCharge(@PathVariable Long chargeId, @RequestBody ChargeDto chargeDto) {
        System.out.println("Entro al update");
        try {
            // Llama al servicio para actualizar el cargo
            Charge updatedCharge = chargeService.updateCharge(chargeId, chargeDto);
            return new ResponseEntity<>(updatedCharge, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/condominium/{condominiumId}")
    public ResponseEntity<List<ChargeSummaryDto>> getChargesByCondominiumId(
            @PathVariable Long condominiumId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        try {
            List<ChargeSummaryDto> charges;
            if (startDate != null && endDate != null) {
                charges = chargeService.getChargesByCondominiumIdAndDateRange(condominiumId, startDate, endDate);
            } else {
                charges = chargeService.getChargesByCondominiumId(condominiumId);
            }

            if (charges.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(charges, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{chargeId}")
    public ResponseEntity<String> logicalDeleteCharge(@PathVariable Long chargeId) {
        try {
            // Llamar al servicio para eliminar el cargo de manera lógica
            chargeService.logicalDeleteCharge(chargeId);
            return ResponseEntity.ok("El cargo y sus pagos asociados fueron eliminados lógicamente.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el cargo.");
        }
    }

}
