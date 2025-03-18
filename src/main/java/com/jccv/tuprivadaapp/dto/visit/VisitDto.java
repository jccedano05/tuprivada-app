package com.jccv.tuprivadaapp.dto.visit;


import com.jccv.tuprivadaapp.model.visit.VisitStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitDto {
    private Long id;
    private String visitorName;
    private String visitorDocument;
    private LocalDateTime visitDate;
    private LocalDateTime expirationDate;
    private String qrToken;
    private VisitStatus status;
    private Long userId; // id del usuario que registra la visita
}
