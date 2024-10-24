package com.jccv.tuprivadaapp.dto.faq.mapper;

import com.jccv.tuprivadaapp.dto.faq.FaqDto;
import com.jccv.tuprivadaapp.model.faq.Faq;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaqMapper {

    private final CondominiumService condominiumService;

    @Autowired
    public FaqMapper(CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public FaqDto toDTO(Faq faq) {
        FaqDto dto = new FaqDto();
        dto.setId(faq.getId());
        dto.setCondominiumId(faq.getCondominium().getId());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        return dto;
    }

    public Faq toEntity(FaqDto dto) {
        Faq faq = new Faq();
        faq.setId(dto.getId());
        faq.setCondominium(condominiumService.findById(dto.getCondominiumId()));
        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        return faq;
    }
}
