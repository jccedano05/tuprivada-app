package com.jccv.tuprivadaapp.controller.accountBank;


import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.MAX_LEVEL;

@RestController
@RequestMapping("accountbank")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class AccountBankController {

    private final AccountBankService accountBankService;

    @Autowired
    public AccountBankController(AccountBankService accountBankService) {
        this.accountBankService = accountBankService;
    }

    @PostMapping
    public ResponseEntity<?> createAccountBank(@RequestBody AccountBankDto accountBankDto) {
        try {

            return new ResponseEntity<>(accountBankService.create(accountBankDto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
