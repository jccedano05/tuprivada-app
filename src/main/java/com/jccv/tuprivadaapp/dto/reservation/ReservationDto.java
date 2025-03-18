package com.jccv.tuprivadaapp.dto.reservation;


import com.jccv.tuprivadaapp.model.reservation.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private Long id;
    private Long amenityId;       // ID de la amenidad a reservar
    private Long residentId;      // ID del residente que realiza la reserva
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ReservationStatus status;
    private BigDecimal cost;
}
