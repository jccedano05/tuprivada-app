package com.jccv.tuprivadaapp.service.finance.implementation;

import com.jccv.tuprivadaapp.dto.finance.FinanceCategoryDto;
import com.jccv.tuprivadaapp.dto.finance.mapper.FinanceCategoryMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.finance.FinanceCategory;
import com.jccv.tuprivadaapp.repository.finance.FinanceCategoryRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.finance.FinanceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceCategoryServiceImp implements FinanceCategoryService {

    private final FinanceCategoryRepository financeCategoryRepository;
    private final FinanceCategoryMapper financeCategoryMapper;
    private final CondominiumService condominiumService;

    @Autowired
    public FinanceCategoryServiceImp(FinanceCategoryRepository financeCategoryRepository, FinanceCategoryMapper financeCategoryMapper, CondominiumService condominiumService) {
        this.financeCategoryRepository = financeCategoryRepository;
        this.financeCategoryMapper = financeCategoryMapper;
        this.condominiumService = condominiumService;
    }

    @Override
    public FinanceCategoryDto createFinanceCategory(FinanceCategoryDto financeCategoryDto) {
        FinanceCategory financeCategory = financeCategoryMapper.toEntity(financeCategoryDto, condominiumService.findById(financeCategoryDto.getCondominiumId()));
        FinanceCategory savedCategory = financeCategoryRepository.save(financeCategory);
        return financeCategoryMapper.toDto(savedCategory);
    }

    @Override
    public FinanceCategoryDto getFinanceCategoryById(Long id) {
        FinanceCategory financeCategory = financeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
        return financeCategoryMapper.toDto(financeCategory);
    }

    @Override
    public List<FinanceCategoryDto> getFinanceCategoriesByCondominium(Long condominiumId) {
        return financeCategoryRepository.findByCondominiumId(condominiumId).stream()
                .map(financeCategoryMapper::toDto).toList();
    }

    @Override
    public FinanceCategoryDto updateFinanceCategory(Long id, FinanceCategoryDto financeCategoryDto) {
        FinanceCategory financeCategory = financeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        financeCategory.setCategory(financeCategoryDto.getCategory());
        financeCategory.setDescription(financeCategoryDto.getDescription());
        financeCategory.setExpense(financeCategoryDto.isExpense());

        FinanceCategory updatedCategory = financeCategoryRepository.save(financeCategory);
        return financeCategoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteFinanceCategory(Long id) {
        FinanceCategory financeCategory = financeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
        financeCategoryRepository.delete(financeCategory);
    }
}
