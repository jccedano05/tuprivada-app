package com.jccv.tuprivadaapp.model.reservation;



import com.jccv.tuprivadaapp.model.amenity.Amenity;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Amenity reservada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenity_id")
    private Amenity amenity;

    // Usuario (residente) que reserva
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    // Rango de fecha y hora de la reserva
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // Estado de la reserva (RESERVED, CANCELLED, COMPLETED)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // Costo de la reserva (calculado a partir de la amenidad, en caso de tener costo)
    private BigDecimal cost;
}
