package com.jccv.tuprivadaapp.service.finance.implementation;


import com.jccv.tuprivadaapp.dto.finance.FinanceDetailBatchItemDto;
import com.jccv.tuprivadaapp.dto.finance.FinanceDetailDto;
import com.jccv.tuprivadaapp.dto.finance.mapper.FinanceDetailMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.model.finance.FinanceCategory;
import com.jccv.tuprivadaapp.model.finance.FinanceDetail;
import com.jccv.tuprivadaapp.repository.finance.FinanceCategoryRepository;
import com.jccv.tuprivadaapp.repository.finance.FinanceDetailRepository;
import com.jccv.tuprivadaapp.repository.finance.FinanceRepository;
import com.jccv.tuprivadaapp.service.finance.FinanceDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceDetailServiceImpl implements FinanceDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceDetailServiceImpl.class);

    private final FinanceDetailRepository financeDetailRepository;

    private final FinanceRepository financeRepository;
    private final FinanceCategoryRepository financeCategoryRepository;

    private final FinanceDetailMapper financeDetailMapper;


    @Autowired
    public FinanceDetailServiceImpl(FinanceDetailRepository financeDetailRepository, FinanceRepository financeRepository, FinanceCategoryRepository financeCategoryRepository, FinanceDetailMapper financeDetailMapper) {
        this.financeDetailRepository = financeDetailRepository;
        this.financeRepository = financeRepository;
        this.financeCategoryRepository = financeCategoryRepository;
        this.financeDetailMapper = financeDetailMapper;
    }

    @Override
    public FinanceDetailDto createFinanceDetail(FinanceDetailDto financeDetailDto) {
        Finance finance = financeRepository.findById(financeDetailDto.getFinanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Finance not found"));
        FinanceCategory financeCategory = financeCategoryRepository.findById(financeDetailDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Finance category not found"));


        FinanceDetail financeDetail = financeDetailMapper.toEntity(financeDetailDto, finance, financeCategory);
        financeDetail = financeDetailRepository.save(financeDetail);
        return financeDetailMapper.toDto(financeDetail);
    }

    @Override
    public FinanceDetailDto getFinanceDetailById(Long id) {
        FinanceDetail financeDetail = financeDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finance detail not found"));
        return financeDetailMapper.toDto(financeDetail);
    }

    @Override
    public List<FinanceDetailDto> getFinanceDetailsByFinance(Long financeId) {
        Finance finance = financeRepository.findById(financeId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance not found"));

        List<FinanceDetail> financeDetails = financeDetailRepository.findByFinanceId(financeId);
        return financeDetails.stream()
                .map(financeDetailMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FinanceDetailDto updateFinanceDetail(Long id, FinanceDetailDto financeDetailDto) {
        // Buscar el FinanceDetail existente
        FinanceDetail financeDetail = financeDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finance detail not found"));

        // Actualizar los campos con los valores del DTO
        financeDetail.setConcept(financeDetailDto.getConcept());
        financeDetail.setAmount(financeDetailDto.getAmount());
        financeDetail.setDescription(financeDetailDto.getDescription());

        // Actualizar las relaciones si están presentes en el DTO
        if (financeDetailDto.getCategoryId() != null) {
            FinanceCategory financeCategory = financeCategoryRepository.findById(financeDetailDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Finance category not found"));
            financeDetail.setFinanceCategory(financeCategory);
        }

        if (financeDetailDto.getFinanceId() != null) {
            Finance finance = financeRepository.findById(financeDetailDto.getFinanceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Finance not found"));
            financeDetail.setFinance(finance);
        }

        // Guardar los cambios en la entidad
        financeDetail = financeDetailRepository.save(financeDetail);

        // Devolver el DTO actualizado
        return financeDetailMapper.toDto(financeDetail);
    }



    @Override
    public void deleteFinanceDetail(Long id) {
        financeDetailRepository.deleteById(id);
    }

    @Override
    public List<FinanceDetailDto> createFinanceDetails(Long financeId, Long categoryId, List<FinanceDetailBatchItemDto> financeDetailDtos) {
        if (financeDetailDtos == null || financeDetailDtos.isEmpty()) {
            throw new BadRequestException("Debes proporcionar al menos un detalle para crear");
        }

        Finance finance = financeRepository.findById(financeId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance not found"));
        FinanceCategory financeCategory = financeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance category not found"));

        if (finance.getCondominium() == null || financeCategory.getCondominium() == null ||
                !finance.getCondominium().getId().equals(financeCategory.getCondominium().getId())) {
            throw new BadRequestException("La categoría debe pertenecer al mismo condominio que la finanza");
        }

        List<FinanceDetail> detailsToPersist = financeDetailDtos.stream()
                .map(dto -> financeDetailMapper.fromBatchItem(dto, finance, financeCategory))
                .collect(Collectors.toList());

        List<FinanceDetail> savedDetails = financeDetailRepository.saveAll(detailsToPersist);
        LOGGER.info("Se registraron {} detalles para la finanza {} y la categoría {}", savedDetails.size(), financeId, categoryId);

        return savedDetails.stream()
                .map(financeDetailMapper::toDto)
                .collect(Collectors.toList());
    }
}
