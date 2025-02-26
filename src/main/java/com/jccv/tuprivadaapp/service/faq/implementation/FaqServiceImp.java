package com.jccv.tuprivadaapp.service.faq.implementation;

import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.faq.FaqDto;
import com.jccv.tuprivadaapp.dto.faq.mapper.FaqMapper;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.faq.Faq;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.repository.faq.FaqRepository;
import com.jccv.tuprivadaapp.service.faq.FaqService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FaqServiceImp implements FaqService {
    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;
    private final CondominiumRepository condominiumRepository;;
    private final PollingNotificationService pollingNotificationService;
    private final OneSignalPushNotificationService oneSignalPushNotificationService;

    @Autowired
    public FaqServiceImp(FaqRepository faqRepository, FaqMapper faqMapper, CondominiumRepository condominiumRepository, PollingNotificationService pollingNotificationService, OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.faqRepository = faqRepository;
        this.faqMapper = faqMapper;
        this.condominiumRepository = condominiumRepository;
        this.pollingNotificationService = pollingNotificationService;
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }

    @Override
    public FaqDto createFaq(FaqDto faqDto) {
        if (!condominiumRepository.existsById(faqDto.getCondominiumId())) {
            throw new IllegalArgumentException("Condominium not found with id " + faqDto.getCondominiumId());
        }
        Faq faq = faqMapper.toEntity(faqDto);
        Faq savedFaq = faqRepository.save(faq);
        pollingNotificationService.createNotificationForCondominium(faqDto.getCondominiumId(), PollingNotificationDto.builder()
                .title(faqDto.getQuestion())
                .message(faqDto.getAnswer())
                .read(false)
                .build());

        oneSignalPushNotificationService.sendPushToCondominium(faq.getCondominium().getId(), PushNotificationRequest.builder()
                .title("Nueva Pregunta Frecuente:")
                .message(faqDto.getQuestion())
                .build());

        return faqMapper.toDTO(savedFaq);
    }

    @Override
    public FaqDto getFaqById(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id " + id));
        return faqMapper.toDTO(faq);
    }

    @Override
    public Page<FaqDto> getFaqsByCondominium(Long condominiumId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return faqRepository.findByCondominiumIdOrderByQuestionAsc(condominiumId, pageable)
                .map(faqMapper::toDTO);
    }

    @Override
    public FaqDto updateFaq(Long id, FaqDto faqDto) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id " + id));
        faq.setQuestion(faqDto.getQuestion());
        faq.setAnswer(faqDto.getAnswer());
        Faq updatedFaq = faqRepository.save(faq);
        return faqMapper.toDTO(updatedFaq);
    }

    @Override
    public void deleteFaq(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id " + id));
        faqRepository.delete(faq);
    }
}
