package com.jccv.tuprivadaapp.dto.notice.mapper;
;
import com.jccv.tuprivadaapp.dto.notice.NoticeDto;
import com.jccv.tuprivadaapp.model.notice.Notice;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NoticeMapper {

    private final CondominiumService condominiumService;
    @Autowired
    public NoticeMapper(CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public NoticeDto toDTO(Notice notice) {
        NoticeDto dto = new NoticeDto();
        dto.setId(notice.getId());
        dto.setCondominiumId(notice.getCondominium().getId());
        dto.setTitle(notice.getTitle());
        dto.setDate(notice.getDate());
        dto.setContent(notice.getContent());
        return dto;
    }

    public Notice toEntity(NoticeDto dto) {

        Notice notice = new Notice();
        notice.setId(dto.getId());
        notice.setCondominium(condominiumService.findById(dto.getCondominiumId()));
        notice.setTitle(dto.getTitle());
        notice.setDate(dto.getDate());
        notice.setContent(dto.getContent());
        return notice;
    }
}