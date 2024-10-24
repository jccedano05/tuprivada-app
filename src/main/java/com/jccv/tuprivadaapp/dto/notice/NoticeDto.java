package com.jccv.tuprivadaapp.dto.notice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
    private Long id;
    private Long condominiumId;
    private String title;
    private LocalDate date;
    private String content;
}
