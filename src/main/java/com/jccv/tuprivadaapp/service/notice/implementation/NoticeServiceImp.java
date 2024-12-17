package com.jccv.tuprivadaapp.service.notice.implementation;

import com.jccv.tuprivadaapp.dto.notice.NoticeDto;
import com.jccv.tuprivadaapp.dto.notice.mapper.NoticeMapper;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.notice.Notice;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.repository.notice.NoticeRepository;
import com.jccv.tuprivadaapp.service.notice.NoticeService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImp implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeMapper noticeMapper;
    private final CondominiumRepository condominiumRepository; // Inyectar el repositorio
    private final PollingNotificationService pollingNotificationService;

    public NoticeServiceImp(NoticeRepository noticeRepository, NoticeMapper noticeMapper, CondominiumRepository condominiumRepository, PollingNotificationService pollingNotificationService) {
        this.noticeRepository = noticeRepository;
        this.noticeMapper = noticeMapper;
        this.condominiumRepository = condominiumRepository; // Inicializa el repositorio
        this.pollingNotificationService = pollingNotificationService;
    }
    @Override
    public NoticeDto createNotice(NoticeDto noticeDTO) {
        if (!condominiumRepository.existsById(noticeDTO.getCondominiumId())) {
            throw new IllegalArgumentException("Condominium not found with id " + noticeDTO.getCondominiumId());
        }
        System.out.println("DTO: " + noticeDTO.getCondominiumId());
        Notice notice = noticeMapper.toEntity(noticeDTO);
        System.out.println("Entity: " + notice.getCondominium().getId());
        Notice savedNotice = noticeRepository.save(notice);
        pollingNotificationService.createNotificationForCondominium(noticeDTO.getCondominiumId(), PollingNotificationDto.builder()
                        .title("Nueva noticia publicada")
                        .message(noticeDTO.getTitle())
                        .read(false)
                .build());
        return noticeMapper.toDTO(savedNotice);
    }

    @Override
    public NoticeDto getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id " + id));
        return noticeMapper.toDTO(notice);
    }

    public Page<NoticeDto> getNoticesByCondominium(Long condominiumId, int page, int size) {Pageable pageable = PageRequest.of(page, size);
        // Usar el método del repositorio para obtener las notificaciones paginadas y ordenadas
        return noticeRepository.findByCondominiumIdOrderByDateDesc(condominiumId, pageable)
                .map(noticeMapper::toDTO); // Mapeo a DTO
    }

    @Override
    public NoticeDto updateNotice(Long id, NoticeDto dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id " + id));

        notice.setTitle(dto.getTitle());
        notice.setDate(dto.getDate());
        notice.setContent(dto.getContent());
        Notice updatedNotice = noticeRepository.save(notice);

        return noticeMapper.toDTO(updatedNotice);
    }

    @Override
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id " + id));
        noticeRepository.delete(notice);

    }
}
