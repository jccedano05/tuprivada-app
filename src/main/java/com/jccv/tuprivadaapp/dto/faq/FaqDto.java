package com.jccv.tuprivadaapp.dto.faq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqDto {
    private Long id;
    private Long condominiumId;
    private String question;
    private String answer;
}
