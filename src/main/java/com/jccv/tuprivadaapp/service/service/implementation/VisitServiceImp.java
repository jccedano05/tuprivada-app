package com.jccv.tuprivadaapp.service.service.implementation;


import com.jccv.tuprivadaapp.dto.visit.VisitDto;
import com.jccv.tuprivadaapp.dto.visit.mapper.VisitMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.visit.Visit;
import com.jccv.tuprivadaapp.model.visit.VisitStatus;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.repository.visit.VisitRepository;
import com.jccv.tuprivadaapp.service.service.VisitService;
import com.jccv.tuprivadaapp.utils.QrTokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitServiceImp implements VisitService {

    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;
    private final UserFacade userFacade; // para obtener el usuario (host)
    private final QrTokenGenerator qrTokenGenerator;

    @Autowired
    public VisitServiceImp(VisitRepository visitRepository, VisitMapper visitMapper, UserFacade userFacade, QrTokenGenerator qrTokenGenerator) {
        this.visitRepository = visitRepository;
        this.visitMapper = visitMapper;
        this.userFacade = userFacade;
        this.qrTokenGenerator = qrTokenGenerator;
    }

    @Override
    public VisitDto createVisit(VisitDto visitDto) {
        // Validar que el usuario (host) exista
        User host = userFacade.findById(visitDto.getUserId());

        // Crear la entidad Visit
        Visit visit = Visit.builder()
                .visitorName(visitDto.getVisitorName())
                .visitorDocument(visitDto.getVisitorDocument())
                .visitDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusHours(2)) // configuración de expiración, por ejemplo 2 horas
                .status(VisitStatus.CREATED)
                .user(host)
                .build();

        System.out.println("Antes del Save");
        System.out.println(visit.toString());

        // Guardar inicialmente para obtener un ID
        visit = visitRepository.save(visit);
        System.out.println("Despues del Save");
        System.out.println(visit.toString());

        // Generar un token seguro para el QR usando el ID de la visita y un secreto
        String qrToken = QrTokenGenerator.generateToken(visit.getId());
        visit.setQrToken(qrToken);

        System.out.println("Despues del token");
        System.out.println(qrToken);

        // Guardar nuevamente con el token asignado
        visit = visitRepository.save(visit);

        return visitMapper.toDto(visit);
    }

    @Override
    public VisitDto validateQrToken(String qrToken) {
        Visit visit = visitRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid QR token"));

        // Verificar si el token ha expirado
        if (visit.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("QR token expired");
        }

        // Si la visita no se ha validado aún, actualizar su estado
        if (visit.getStatus() != VisitStatus.VALIDATED) {
            visit.setStatus(VisitStatus.VALIDATED);
            visit = visitRepository.save(visit);
        }
        return visitMapper.toDto(visit);
    }

    @Override
    public List<VisitDto> getActiveVisits(Long userId) {
        // Visitas activas: estado CREATED y fecha de expiración en el futuro
        List<Visit> activeVisits = visitRepository.findByUserIdAndStatusAndExpirationDateAfter(
                userId, VisitStatus.CREATED, LocalDateTime.now());
        return activeVisits.stream()
                .map(visitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitDto> getAnnualHistory(Long userId, int year) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        List<Visit> history = visitRepository.findByUserIdAndVisitDateBetweenOrderByVisitDateDesc(
                userId, start, end);
        return history.stream()
                .map(visitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitDto> getDailyVisits(Long condominiumId, LocalDateTime start, LocalDateTime end) {
        List<Visit> dailyVisits = visitRepository.findByUser_Condominium_IdAndVisitDateBetweenOrderByVisitDateAsc(
                condominiumId, start, end);
        return dailyVisits.stream()
                .map(visitMapper::toDto)
                .collect(Collectors.toList());
    }

}
