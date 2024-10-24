package com.jccv.tuprivadaapp.controller.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceDto;
import com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto;
import com.jccv.tuprivadaapp.service.finance.FinanceService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/finances")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping
    public ResponseEntity<FinanceDto> createFinance(@RequestBody FinanceDto financeDto) {
        FinanceDto createdFinance = financeService.createFinance(financeDto);
        return ResponseEntity.ok(createdFinance);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinanceDto> getFinanceById(@PathVariable Long id) {
        FinanceDto financeDto = financeService.getFinanceById(id);
        return ResponseEntity.ok(financeDto);
    }

    @GetMapping("/condominiums/{condominiumId}")
    public Page<FinanceDto> getFinancesByCondominium(
            @PathVariable Long condominiumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return financeService.getFinancesByCondominium(condominiumId, page, size);
    }

    @GetMapping("/condominiums/{condominiumId}/years/{year}")
    public List<FinanceDto> getFinancesByYear(@PathVariable Long condominiumId, @PathVariable int year) {
        return financeService.getFinancesCondominiumByYear(condominiumId, year);
    }

    @GetMapping("/annual/{condominiumId}")
    public List<AnnualFinanceDto> getAnnualFinances(@PathVariable Long condominiumId) {
        return financeService.getAnnualFinances(condominiumId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinanceDto> updateFinance(@PathVariable Long id, @RequestBody FinanceDto financeDto) {
        FinanceDto updatedFinance = financeService.updateFinance(id, financeDto);
        return ResponseEntity.ok(updatedFinance);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinance(@PathVariable Long id) {
        financeService.deleteFinance(id);
        return ResponseEntity.noContent().build();
    }
}
