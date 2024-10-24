package com.jccv.tuprivadaapp.controller.notice;



import com.jccv.tuprivadaapp.dto.notice.NoticeDto;
import com.jccv.tuprivadaapp.service.notice.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.USER_LEVEL;

@RestController
@RequestMapping("/notices")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeDto noticeDto) {  // Cambiado a NoticeDto
        try{
            NoticeDto createdNotice = noticeService.createNotice(noticeDto);
            return ResponseEntity.ok(createdNotice);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize(USER_LEVEL)
    public ResponseEntity<NoticeDto> getNoticeById(@PathVariable Long id) {  // Cambiado a NoticeDto
        NoticeDto noticeDto = noticeService.getNoticeById(id);
        return ResponseEntity.ok(noticeDto);
    }

    @GetMapping("/condominiums/{condominiumId}")
    @PreAuthorize(USER_LEVEL)
    public Page<NoticeDto> getNoticesByCondominium(
            @PathVariable Long condominiumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return noticeService.getNoticesByCondominium(condominiumId, page, size);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeDto> updateNotice(@PathVariable Long id, @RequestBody NoticeDto noticeDto) {  // Cambiado a NoticeDto
        NoticeDto updatedNotice = noticeService.updateNotice(id, noticeDto);
        return ResponseEntity.ok(updatedNotice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
