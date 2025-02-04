package com.jccv.tuprivadaapp.model.calendar;

import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDate date; // Formato: YYYY-MM-DD
    private String startTime;  // Formato: HH:MM
    private String endTime;  // Formato: HH:MM
    private String location;

    @ManyToOne
    @JoinColumn(name = "condominium_id")
    private Condominium condominium;
}
