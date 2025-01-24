package com.jccv.tuprivadaapp.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private String startTime;  // Formato: HH:MM
    private String endTime;  // Formato: HH:MM
    private Long condominiumId; // Agregar el ID del condominio
}
