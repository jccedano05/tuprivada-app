package com.jccv.tuprivadaapp.repository.charge;


import com.jccv.tuprivadaapp.model.charge.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    // Solo traerá los cargos activos
    List<Charge> findByCondominiumIdAndIsActiveTrueOrderByChargeDateDesc(Long condominiumId);

    // Solo traerá los cargos activos dentro del rango de fechas
    List<Charge> findByCondominiumIdAndChargeDateBetweenAndIsActiveTrueOrderByChargeDateDesc(Long condominiumId, LocalDateTime startDate, LocalDateTime endDate);
}
