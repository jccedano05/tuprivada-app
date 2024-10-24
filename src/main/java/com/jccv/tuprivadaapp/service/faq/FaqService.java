package com.jccv.tuprivadaapp.service.faq;

import com.jccv.tuprivadaapp.dto.faq.FaqDto;
import org.springframework.data.domain.Page;

public interface FaqService {
    FaqDto createFaq(FaqDto faqDto);
    FaqDto getFaqById(Long id);
    Page<FaqDto> getFaqsByCondominium(Long condominiumId, int page, int size);
    FaqDto updateFaq(Long id, FaqDto faqDto);
    void deleteFaq(Long id);
}
