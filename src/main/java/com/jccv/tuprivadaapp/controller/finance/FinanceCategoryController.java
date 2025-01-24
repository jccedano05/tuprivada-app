package com.jccv.tuprivadaapp.controller.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceCategoryDto;
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
    public ResponseEntity<List<FinanceCategoryDto>> getAllCategoriesByCondominium(@PathVariable Long condominiumId) {
        List<FinanceCategoryDto> categories = financeCategoryService.getFinanceCategoriesByCondominium(condominiumId);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<FinanceCategoryDto> createCategory(@RequestBody FinanceCategoryDto financeCategoryDTO) {
        FinanceCategoryDto createdCategory = financeCategoryService.createFinanceCategory(financeCategoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinanceCategoryDto> updateCategory(@PathVariable Long id, @RequestBody FinanceCategoryDto financeCategoryDTO) {
        FinanceCategoryDto updatedCategory = financeCategoryService.updateFinanceCategory(id, financeCategoryDTO);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        financeCategoryService.deleteFinanceCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
