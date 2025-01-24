package com.jccv.tuprivadaapp.service.finance.implementation;

import com.jccv.tuprivadaapp.dto.finance.FinanceDto;
import com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto;
import com.jccv.tuprivadaapp.dto.finance.FinanceSummaryDto;
import com.jccv.tuprivadaapp.dto.finance.mapper.FinanceMapper;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.repository.finance.FinanceRepository;
import com.jccv.tuprivadaapp.service.finance.FinanceService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import com.jccv.tuprivadaapp.utils.DateFormats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceServiceImp implements FinanceService {

    private final FinanceRepository financeRepository;
    private final FinanceMapper financeMapper;
    private final CondominiumRepository condominiumRepository;
    private final PollingNotificationService pollingNotificationService;
    private final DateFormats dateFormats;

    public FinanceServiceImp(FinanceRepository financeRepository, FinanceMapper financeMapper, CondominiumRepository condominiumRepository, PollingNotificationService pollingNotificationService, DateFormats dateFormats) {
        this.financeRepository = financeRepository;
        this.financeMapper = financeMapper;
        this.condominiumRepository = condominiumRepository;
        this.pollingNotificationService = pollingNotificationService;
        this.dateFormats = dateFormats;
    }

    @Override
    public FinanceDto createFinance(FinanceDto financeDto) {
        if (!condominiumRepository.existsById(financeDto.getCondominiumId())) {
            throw new IllegalArgumentException("Condominium not found with id " + financeDto.getCondominiumId());
        }
        Finance finance = financeMapper.toEntity(financeDto);
        Finance savedFinance = financeRepository.save(finance);
        pollingNotificationService.createNotificationForCondominium(financeDto.getCondominiumId(), PollingNotificationDto.builder()
                .title("Reporte mensual de finanzas")
                .message("Se creo el reporte de finanzas correspondiente a " + dateFormats.getMonthAndYear(financeDto.getDate()))
                .read(false)
                .build());
        return financeMapper.toDTO(savedFinance);
    }

    @Override
    public FinanceDto getFinanceById(Long id) {
        Finance finance = financeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finance not found with id " + id));
        return financeMapper.toDTO(finance);
    }

    @Override
    public Page<FinanceDto> getFinancesByCondominium(Long condominiumId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return financeRepository.findByCondominiumIdOrderByDateDesc(condominiumId, pageable)
                .map(financeMapper::toDTO);
    }

    public List<FinanceSummaryDto> getFinancesCondominiumByYear(Long condominiumId, int year) {
//        return financeRepository.findFinancesByYear(condominiumId, year).stream().map(finance -> financeMapper.toDTO(finance)).toList();
        return financeRepository.findFinanceSummaryByCondominiumAndYear(condominiumId, year);
    }

    @Override
    public List<AnnualFinanceDto> getAnnualFinances(Long condominiumId) {
        return financeRepository.findFinancesGroupedByYear(condominiumId);
    }

    @Override
    public FinanceDto updateFinance(Long id, FinanceDto financeDto) {
        Finance finance = financeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finance not found with id " + id));

        Finance updatedFinance = financeRepository.save(financeMapper.toEntity(financeDto));
        return financeMapper.toDTO(updatedFinance);
    }

    @Override
    public void deleteFinance(Long id) {
        Finance finance = financeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finance not found with id " + id));
        financeRepository.delete(finance);
    }
}
