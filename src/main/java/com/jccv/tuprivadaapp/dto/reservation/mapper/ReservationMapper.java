package com.jccv.tuprivadaapp.dto.reservation.mapper;

import com.jccv.tuprivadaapp.dto.reservation.ReservationDto;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.amenity.Amenity;
import com.jccv.tuprivadaapp.model.reservation.Reservation;
import com.jccv.tuprivadaapp.model.resident.Resident;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {


    public ReservationDto toDto(Reservation reservation){
        return ReservationDto.builder()
                .id(reservation.getId())
                .amenityId(reservation.getAmenity().getId())
                .cost(reservation.getCost())
                .endDateTime(reservation.getEndDateTime())
                .residentId(reservation.getResident().getId())
                .startDateTime(reservation.getStartDateTime())
                .status(reservation.getStatus())
                .build();
    }

    public Reservation toEntity(ReservationDto reservation, Resident resident, Amenity amenity){
        return Reservation.builder()
                .id(reservation.getId())
                .amenity(amenity)
                .cost(reservation.getCost())
                .endDateTime(reservation.getEndDateTime())
                .resident(resident)
                .startDateTime(reservation.getStartDateTime())
                .status(reservation.getStatus())
                .build();
    }
}
