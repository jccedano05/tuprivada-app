package com.jccv.tuprivadaapp.service.condominium;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.dto.condominium.CondominiumDto;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;

import java.util.List;

public interface CondominiumService {

    public Condominium create(CondominiumDto condominiumDto);

    public Condominium findById(Long id);

    public List<Condominium> findAll();

    public Condominium update(CondominiumDto condominiumDto);

    public void deleteById(Long id);

}
