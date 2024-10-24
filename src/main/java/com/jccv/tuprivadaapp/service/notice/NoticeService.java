package com.jccv.tuprivadaapp.service.notice;

import com.jccv.tuprivadaapp.dto.notice.NoticeDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NoticeService {
    public NoticeDto createNotice(NoticeDto noticeDTO);

    public NoticeDto getNoticeById(Long id);
    public Page<NoticeDto> getNoticesByCondominium(Long condominiumId, int page, int size);
    public NoticeDto updateNotice(Long id, NoticeDto dto);

    public void deleteNotice(Long id);
}
