package com.jccv.tuprivadaapp.controller.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceCategoryDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.service.finance.FinanceCategoryService;
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
@RequestMapping("/finance-categories")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class FinanceCategoryController {

    private final FinanceCategoryService financeCategoryService;

    @Autowired
    public FinanceCategoryController(FinanceCategoryService financeCategoryService) {
        this.financeCategoryService = financeCategoryService;
    }

    @PreAuthorize(RESIDENT_LEVEL)
    @GetMapping("/condominiums/{condominiumId}")
    public ResponseEntity<?> getAllCategoriesByCondominium(@PathVariable Long condominiumId) {
       try{
           List<FinanceCategoryDto> categories = financeCategoryService.getFinanceCategoriesByCondominium(condominiumId);
           return new ResponseEntity<>(categories, HttpStatus.OK);
       } catch (ResourceNotFoundException e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
       }catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }


    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody FinanceCategoryDto financeCategoryDTO) {
        try{
            FinanceCategoryDto createdCategory = financeCategoryService.createFinanceCategory(financeCategoryDTO);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody FinanceCategoryDto financeCategoryDTO) {
        try{
            FinanceCategoryDto updatedCategory = financeCategoryService.updateFinanceCategory(id, financeCategoryDTO);
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
       try{
           financeCategoryService.deleteFinanceCategory(id);
           return new ResponseEntity<>(HttpStatus.NO_CONTENT);
       }catch (ResourceNotFoundException e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
       }catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }
}
