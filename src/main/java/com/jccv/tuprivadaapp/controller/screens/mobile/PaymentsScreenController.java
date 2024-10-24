package com.jccv.tuprivadaapp.controller.screens.mobile;

import com.jccv.tuprivadaapp.service.screens.mobile.PaymentsScreenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("paymentsMobileScreen")
public class PaymentsScreenController {


    private final PaymentsScreenService paymentsScreenService;

    public PaymentsScreenController(PaymentsScreenService paymentsScreenService) {
        this.paymentsScreenService = paymentsScreenService;
    }

    @GetMapping("resident")
    public ResponseEntity<?> getHomeInformationResident() {
        try {
            return new ResponseEntity<>(paymentsScreenService.getResidentPaymetsScreenInformation(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
