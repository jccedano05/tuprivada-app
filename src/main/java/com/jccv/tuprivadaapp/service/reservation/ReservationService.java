// File: com/jccv/tuprivadaapp/service/reservation/ReservationService.java
package com.jccv.tuprivadaapp.service.reservation;

import com.jccv.tuprivadaapp.dto.reservation.ReservationDto;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    ReservationDto createReservation(ReservationDto dto);
    ReservationDto updateReservation(Long id, ReservationDto dto);
    boolean cancelReservation(Long id);

    // Obtiene reservas para un residente en un día específico
    List<ReservationDto> getReservationsByResidentAndDate(Long residentId, LocalDate date);

    // Obtiene reservas para un condominio en un día específico
    List<ReservationDto> getReservationsByCondominiumAndDate(Long condominiumId, LocalDate date);
    public void confirmReservationPayment(Long reservationId, Long residentId, Boolean isPaid);


    List<ReservationDto> getActiveReservationsByResident(Long residentId);

    List<ReservationDto> getReservationHistoryByResidentAndYear(Long residentId, int year);


    // Obtener reservas activas de un condominio por mes
    List<ReservationDto> getActiveReservationsByCondominiumAndMonth(Long condominiumId, int year, int month);

    // Obtener historial de un condominio por mes
    List<ReservationDto> getReservationHistoryByCondominiumAndMonth(Long condominiumId, int year, int month);


}
