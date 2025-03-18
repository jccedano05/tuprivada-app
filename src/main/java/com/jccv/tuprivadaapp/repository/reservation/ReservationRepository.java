package com.jccv.tuprivadaapp.repository.reservation;

import com.jccv.tuprivadaapp.model.reservation.Reservation;
import com.jccv.tuprivadaapp.model.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Buscar las reservas de un residente en un rango de fechas
    List<Reservation> findByResident_IdAndStartDateTimeBetween(Long residentId, LocalDateTime start, LocalDateTime end);

    // Buscar reservas en un rango de fechas para un condominio específico
    List<Reservation> findByAmenity_Condominium_IdAndStartDateTimeBetween(Long condominiumId, LocalDateTime start, LocalDateTime end);

    // Buscar reservas de una amenidad dentro de un rango de tiempo
    List<Reservation> findByAmenity_IdAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqual(Long amenityId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    // Buscar una reserva activa de una amenidad para el residente en el mismo día
    Optional<Reservation> findByAmenity_IdAndResident_IdAndStartDateTimeBetween(Long amenityId, Long residentId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Obtener todas las reservas activas de un residente ordenadas por fecha más cercana al día actual
    List<Reservation> findByResident_IdAndStartDateTimeGreaterThanEqualAndStatusNotInOrderByStartDateTimeAsc(Long residentId, LocalDateTime now, List<ReservationStatus> excludedStatuses);

    // Buscar reservas completadas o canceladas de un residente por año
    List<Reservation> findByResident_IdAndStartDateTimeBetweenAndStatusIn(Long residentId, LocalDateTime start, LocalDateTime end, List<ReservationStatus> statuses);

    // Obtener todas las reservas activas de un condominio, dentro de un rango de fechas,
// excluyendo aquellas cuya fecha de inicio sea más antigua que la fecha actual
// y que no estén en los estados CANCELLED o COMPLETED.
    List<Reservation> findByAmenity_Condominium_IdAndStartDateTimeBetweenAndStartDateTimeGreaterThanEqualAndStatusNotInOrderByStartDateTimeAsc(
            Long condominiumId, LocalDateTime start, LocalDateTime end, LocalDateTime now, List<ReservationStatus> excludedStatuses);

    // Buscar historial de reservas completadas o canceladas de un condominio por mes
    List<Reservation> findByAmenity_Condominium_IdAndStartDateTimeBetweenAndStatusIn(Long condominiumId, LocalDateTime start, LocalDateTime end, List<ReservationStatus> statuses);



}
