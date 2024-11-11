package com.jccv.tuprivadaapp.repository.charge;


import com.jccv.tuprivadaapp.model.charge.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByCondominiumIdOrderByChargeDateDesc(Long condominiumId);

    List<Charge> findByCondominiumIdAndChargeDateBetweenOrderByChargeDateDesc(Long condominiumId, LocalDateTime startDate, LocalDateTime endDate);
}
