package com.jccv.tuprivadaapp.repository.visit;


import com.jccv.tuprivadaapp.model.visit.Visit;
import com.jccv.tuprivadaapp.model.visit.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findByQrToken(String qrToken);
    // Visitas activas: estado CREATED y fecha de expiración posterior al momento actual
    List<Visit> findByUserIdAndStatusAndExpirationDateAfter(Long hostId, VisitStatus status, LocalDateTime now);

    // Historial anual: visitas entre el inicio y el final del año, ordenadas de forma descendente por visitDate
    @Modifying
    @Transactional
    @Query("""
    UPDATE Visit v
    SET v.status = 'EXPIRED'
    WHERE v.status IN ('CREATED', 'VALIDATED')
    AND v.visitDate < FUNCTION('DATE_TRUNC', 'day', CURRENT_TIMESTAMP)
    AND v.user.id = :userId
    AND v.visitDate BETWEEN :start AND :end
""")
    List<Visit> findByUserIdAndVisitDateBetweenOrderByVisitDateDesc(Long userId, LocalDateTime start, LocalDateTime end);

    // Visitas diarias: se consultan por el id del condominio (a través del host) y un rango de fecha (inicio y fin del día)
    @Modifying
    @Transactional
    @Query("""
    UPDATE Visit v
    SET v.status = 'EXPIRED'
    WHERE v.status IN ('CREATED', 'VALIDATED')
    AND v.visitDate < FUNCTION('DATE_TRUNC', 'day', CURRENT_TIMESTAMP)
    AND v.condominium.id = :condominiumId
    AND v.visitDate BETWEEN :start AND :end
""")
    List<Visit> findByUser_Condominium_IdAndVisitDateBetweenOrderByVisitDateAsc(Long condominiumId, LocalDateTime start, LocalDateTime end);
}
