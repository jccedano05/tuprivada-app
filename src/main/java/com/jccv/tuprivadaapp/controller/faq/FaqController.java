package com.jccv.tuprivadaapp.controller.faq;

import com.jccv.tuprivadaapp.dto.faq.FaqDto;
import com.jccv.tuprivadaapp.service.faq.FaqService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.USER_LEVEL;

@RestController
@RequestMapping("/faqs")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @PostMapping
    public ResponseEntity<?> createFaq(@RequestBody FaqDto faqDto) {
        try {
            FaqDto createdFaq = faqService.createFaq(faqDto);
            return ResponseEntity.ok(createdFaq);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize(USER_LEVEL)
    public ResponseEntity<FaqDto> getFaqById(@PathVariable Long id) {
        FaqDto faqDto = faqService.getFaqById(id);
        return ResponseEntity.ok(faqDto);
    }

    @GetMapping("/condominiums/{condominiumId}")
    @PreAuthorize(USER_LEVEL)
    public Page<FaqDto> getFaqsByCondominium(
            @PathVariable Long condominiumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return faqService.getFaqsByCondominium(condominiumId, page, size);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaqDto> updateFaq(@PathVariable Long id, @RequestBody FaqDto faqDto) {
        FaqDto updatedFaq = faqService.updateFaq(id, faqDto);
        return ResponseEntity.ok(updatedFaq);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}
