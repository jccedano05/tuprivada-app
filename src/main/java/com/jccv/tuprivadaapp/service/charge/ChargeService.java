package com.jccv.tuprivadaapp.service.charge;

import com.jccv.tuprivadaapp.dto.charge.ChargeDto;
import com.jccv.tuprivadaapp.dto.charge.ChargeSummaryDto;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface ChargeService {

    public Charge createCharge(ChargeDto chargeDto);

    Charge findById(Long chargeId);

    List<ChargeSummaryDto> getChargesByCondominiumId(Long condominiumId);

    List<ChargeSummaryDto> getChargesByCondominiumIdAndDateRange(Long condominiumId, LocalDateTime startDate, LocalDateTime endDate);


}
