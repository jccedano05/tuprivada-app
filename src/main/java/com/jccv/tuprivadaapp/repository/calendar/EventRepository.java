package com.jccv.tuprivadaapp.repository.calendar;

import com.jccv.tuprivadaapp.model.calendar.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Buscar eventos por condominio y mes/año sin importar el día
    @Query("SELECT e FROM Event e WHERE e.condominium.id = :condominiumId " +
            "AND EXTRACT(MONTH FROM e.date) = :month " +
            "AND EXTRACT(YEAR FROM e.date) = :year")
    List<Event> findByCondominiumIdAndMonth(@Param("condominiumId") Long condominiumId,
                                            @Param("month") int month,
                                            @Param("year") int year);
}
