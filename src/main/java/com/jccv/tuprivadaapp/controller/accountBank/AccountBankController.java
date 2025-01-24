package com.jccv.tuprivadaapp.controller.accountBank;


import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.*;

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
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize(RESIDENT_LEVEL)
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountBankById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(accountBankService.findById(id), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las cuentas bancarias por ID de condominio
    @PreAuthorize(RESIDENT_LEVEL)
    @GetMapping("/condominiums/{condominiumId}")
    public ResponseEntity<?> getAllAccountBanksByCondominiumId(@PathVariable Long condominiumId) {
        try {
            return new ResponseEntity<>(accountBankService.findAllByCondominiumId(condominiumId), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar una cuenta bancaria por ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccountBank(@PathVariable Long id, @RequestBody AccountBankDto accountBankDto) {
        try {
            return new ResponseEntity<>(accountBankService.update(id, accountBankDto), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar una cuenta bancaria por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccountBank(@PathVariable Long id) {
        try {
            accountBankService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

