package com.jccv.tuprivadaapp.service.reservation.implementation;


import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.dto.reservation.ReservationDto;
import com.jccv.tuprivadaapp.dto.reservation.mapper.ReservationMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.amenity.Amenity;
import com.jccv.tuprivadaapp.model.reservation.Reservation;
import com.jccv.tuprivadaapp.model.reservation.ReservationStatus;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.amenity.AmenityRepository;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.repository.reservation.ReservationRepository;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import com.jccv.tuprivadaapp.service.reservation.ReservationService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImp implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AmenityRepository amenityRepository;
    private final ResidentService residentService;
    private final ReservationMapper reservationMapper;
    private final PollingNotificationService pollingNotificationService;
    private final OneSignalPushNotificationService oneSignalPushNotificationService;

    @Autowired
    public ReservationServiceImp(ReservationRepository reservationRepository, AmenityRepository amenityRepository, ResidentService residentService, ReservationMapper reservationMapper, PollingNotificationService pollingNotificationService, OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.reservationRepository = reservationRepository;
        this.amenityRepository = amenityRepository;
        this.reservationMapper = reservationMapper;
        this.residentService = residentService;
        this.pollingNotificationService = pollingNotificationService;
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }

    @Override
    public ReservationDto createReservation(ReservationDto dto) {
        // Buscar la amenidad a reservar
        Amenity amenity = amenityRepository.findById(dto.getAmenityId())
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));

        // Buscar el residente (usuario)
        Resident resident = residentService.getResidentById(dto.getResidentId()).orElseThrow(() -> new BadRequestException("No se encontro el residente con el Id: " + dto.getResidentId()));

        // Obtener el inicio y fin del día de la reserva
        LocalDateTime startOfDay = dto.getStartDateTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = dto.getStartDateTime().toLocalDate().atTime(23, 59, 59);

        // Verificar si el residente ya tiene una reserva para la misma amenidad en el mismo día
        Optional<Reservation> existingReservation = reservationRepository.findByAmenity_IdAndResident_IdAndStartDateTimeBetween(
                dto.getAmenityId(), dto.getResidentId(), startOfDay, endOfDay);



        // Verificar si el horario está disponible
        if (!isAvailable(dto.getStartDateTime(), dto.getEndDateTime(), dto.getAmenityId())) {
            throw new BadRequestException("El horario seleccionado no está disponible.");
        }

        if (existingReservation.isPresent()) {
            throw new BadRequestException("Ya tienes una reserva para esta amenidad en el día seleccionado. Modifica o elimina la reserva existente.");
        }

        // Calcular el costo si la amenidad tiene costo
        BigDecimal cost = amenity.getCost() != null && amenity.getCost().compareTo(BigDecimal.ZERO) > 0
                ? amenity.getCost()
                : BigDecimal.ZERO;

        // Crear la reserva
        Reservation reservation = Reservation.builder()
                .amenity(amenity)
                .resident(resident)
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .status(ReservationStatus.REQUESTED)
                .cost(cost)
                .build();

        // Guardar la reserva
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    @Override
    public ReservationDto updateReservation(Long id, ReservationDto dto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        reservation.setStartDateTime(dto.getStartDateTime());
        reservation.setEndDateTime(dto.getEndDateTime());
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    @Override
    public boolean cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public List<ReservationDto> getReservationsByResidentAndDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        List<Reservation> reservations = reservationRepository.findByResident_IdAndStartDateTimeBetween(userId, start, end);
        return reservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getReservationsByCondominiumAndDate(Long condominiumId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        List<Reservation> reservations = reservationRepository.findByAmenity_Condominium_IdAndStartDateTimeBetween(condominiumId, start, end);
        return reservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void confirmReservationPayment(Long reservationId, Long residentId, Boolean isPaid) {
        Resident resident = residentService.getResidentById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró al residente con el ID: " + residentId));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada para residentId: " + residentId));

        if (reservation.getStatus() != ReservationStatus.REQUESTED) {
            throw new BadRequestException("Solo se pueden confirmar reservas en estado REQUESTED.");
        }

        double reservationCost = reservation.getAmenity().getCost().doubleValue(); // Si el amenity tiene un costo

        double balanceAfterPaid = resident.getBalance() - reservationCost;

        if (isPaid && balanceAfterPaid < 0) {
            throw new BadRequestException("Saldo insuficiente para hacer el pago.");
        }

        if (isPaid) {
            reservation.setStatus(ReservationStatus.PAID);

            // Crear una notificación
            pollingNotificationService.createNotification(PollingNotificationDto.builder()
                    .title("Pago de reserva exitoso")
                    .message("Tu reserva para " + reservation.getAmenity().getName() + " ha sido confirmada.")
                    .userId(resident.getUser().getId())
                    .read(false)
                    .build());

            // Enviar push notification
            oneSignalPushNotificationService.sendPushToUser(PushNotificationRequest.builder()
                    .title("Reserva confirmada")
                    .message("Tu reserva ha sido pagada y confirmada.")
                    .userId(resident.getUser().getId())
                    .build());

        } else {
            throw new BadRequestException("El pago de la reserva ha sido rechazado.");
        }

        reservationRepository.save(reservation);
        residentService.updateBalanceResident(resident, -reservationCost);
    }

    public boolean isAvailable(LocalDateTime startDateTime, LocalDateTime endDateTime, Long amenityId) {
        List<Reservation> reservations = reservationRepository.findByAmenity_IdAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqual(
                amenityId, endDateTime, startDateTime
        );
        return reservations.isEmpty();
    }

//    @Override
//    public List<ReservationDto> getActiveReservationsByResident(Long residentId) {
//        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
//        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
//        List<Reservation> activeReservations = reservationRepository.findByResident_IdAndStartDateTimeBetween(
//                residentId, startOfDay, endOfDay);
//
//        // Actualizar el estado de las reservas que han pasado su fecha
////        updateExpiredReservationsStatus();
//
//        return activeReservations.stream()
//                .map(reservationMapper::toDto)
//                .collect(Collectors.toList());
//    }

    @Override
    public List<ReservationDto> getActiveReservationsByResident(Long residentId) {
        // Obtener la fecha y hora actual (incluyendo el inicio del día)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // Buscar reservas del residente que comienzan desde el inicio del día hasta una fecha futura
        List<Reservation> activeReservations = reservationRepository.findByResident_IdAndStartDateTimeGreaterThanEqualAndStatusNotInOrderByStartDateTimeAsc(
                residentId, startOfDay, List.of(ReservationStatus.COMPLETED, ReservationStatus.CANCELLED));

        // Mapear las reservas a DTOs y devolver la lista
        return activeReservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getReservationHistoryByResidentAndYear(Long residentId, int year) {
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        List<Reservation> reservations = reservationRepository.findByResident_IdAndStartDateTimeBetweenAndStatusIn(
                residentId, startOfYear, endOfYear, List.of(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED)
        );
        return reservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getActiveReservationsByCondominiumAndMonth(Long condominiumId, int year, int month) {

        LocalDateTime now = LocalDateTime.now();  // Fecha y hora actual
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        List<Reservation> reservations = reservationRepository.findByAmenity_Condominium_IdAndStartDateTimeBetweenAndStartDateTimeGreaterThanEqualAndStatusNotInOrderByStartDateTimeAsc(
                condominiumId, startOfMonth, endOfMonth, now, List.of(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED)
        );
        return reservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getReservationHistoryByCondominiumAndMonth(Long condominiumId, int year, int month) {

        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        List<Reservation> reservations = reservationRepository.findByAmenity_Condominium_IdAndStartDateTimeBetweenAndStatusIn(
                condominiumId, startOfMonth, endOfMonth, List.of(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED)
        );
        return reservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }



//    @Override
//    public List<ReservationDto> getReservationHistoryByResidentAndYear(Long residentId, int year) {
//        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
//        LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
//        List<Reservation> historyReservations = reservationRepository.findByResident_IdAndStartDateTimeBetween(
//                residentId, startOfYear, endOfYear);
//
//        return historyReservations.stream()
//                .map(reservationMapper::toDto)
//                .collect(Collectors.toList());
//    }



//    @Transactional
//    @Override
//    public void updateExpiredReservationsStatus() {
//        LocalDateTime now = LocalDateTime.now();
//        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
//                .filter(reservation -> reservation.getEndDateTime().isBefore(now)
//                        && reservation.getStatus() != ReservationStatus.CANCELLED
//                        && reservation.getStatus() != ReservationStatus.COMPLETED)
//                .toList();
//
//        expiredReservations.forEach(reservation -> {
//            reservation.setStatus(ReservationStatus.COMPLETED);
//            reservationRepository.save(reservation);
//        });
//    }

    @Scheduled(cron = "0 0 0 * * ?")  // Ejecuta a medianoche todos los días
    public void updateExpiredReservationsStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getEndDateTime().isBefore(now)
                        && reservation.getStatus() != ReservationStatus.CANCELLED
                        && reservation.getStatus() != ReservationStatus.COMPLETED)
                .toList();

        expiredReservations.forEach(reservation -> {
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
        });
    }

}
