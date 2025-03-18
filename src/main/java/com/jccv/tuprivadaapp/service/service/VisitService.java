package com.jccv.tuprivadaapp.service.service;


import com.jccv.tuprivadaapp.dto.visit.VisitDto;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitService {
    VisitDto createVisit(VisitDto visitDto);
    VisitDto validateQrToken(String qrToken);

    // Obtiene las visitas activas de un residente
    List<VisitDto> getActiveVisits(Long userId);

    // Obtiene el historial anual de visitas para un residente, ordenado de la fecha más reciente a la más antigua
    List<VisitDto> getAnnualHistory(Long userId, int year);

    // Obtener visitas diarias de un condominio
    List<VisitDto> getDailyVisits(Long condominiumId, LocalDateTime start, LocalDateTime end);


}
