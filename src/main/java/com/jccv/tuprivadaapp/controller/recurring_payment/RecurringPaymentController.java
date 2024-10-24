package com.jccv.tuprivadaapp.controller.recurring_payment;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.dto.recurringPayment.RecurringPaymentDto;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import com.jccv.tuprivadaapp.service.recurring_payment.RecurringPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;

@RestController
@RequestMapping("recurringPayment")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    @Autowired
    public RecurringPaymentController(RecurringPaymentService recurringPaymentService) {
        this.recurringPaymentService = recurringPaymentService;
    }

    @PostMapping
    public ResponseEntity<?> createAccountBank(@RequestBody RecurringPaymentDto recurringPaymentDto) {
        try {

            return new ResponseEntity<>(recurringPaymentService.create(recurringPaymentDto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
