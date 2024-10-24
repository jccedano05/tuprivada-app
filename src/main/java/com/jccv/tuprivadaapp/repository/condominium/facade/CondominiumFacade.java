package com.jccv.tuprivadaapp.repository.condominium.facade;

import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CondominiumFacade {

    private final CondominiumRepository condominiumRepository;

    @Autowired
    public CondominiumFacade(CondominiumRepository condominiumRepository) {
        this.condominiumRepository = condominiumRepository;
    }

    public Condominium getAccountBankById(Long condominiumId){
        return condominiumRepository.findById(condominiumId).orElseThrow(() -> new BadRequestException("No se pudo encontrar el condominio con el id: " + condominiumId));
    }
}
