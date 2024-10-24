package com.jccv.tuprivadaapp.service.accountBank.implementation;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.dto.acoountBank.mapper.AccountBankMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.account_bank.AccountBank;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.accountBank.AccountBankRepository;
import com.jccv.tuprivadaapp.service.accountBank.AccountBankService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import com.jccv.tuprivadaapp.utils.UserSessionInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountBankServiceImp implements AccountBankService {

    private final AccountBankRepository accountBankRepository;
    private final AccountBankMapper accountBankMapper;

    private final UserSessionInformation userSessionInformation;

    private final ResidentService residentService;

@Autowired
    public AccountBankServiceImp(AccountBankRepository accountBankRepository, AccountBankMapper accountBankMapper, UserSessionInformation userSessionInformation, ResidentService residentService) {
        this.accountBankRepository = accountBankRepository;
        this.accountBankMapper = accountBankMapper;
        this.userSessionInformation = userSessionInformation;
        this.residentService = residentService;
    }

    @Override
    @Transactional
    public AccountBankDto create(AccountBankDto accountBankDto) {
        User user = userSessionInformation.getUserInformationFromSecurityContext();
        //Only Superadmin or Admin with the same condominium can create
        if(user.getRole() != Role.SUPERADMIN && user.getCondominium().getId() != accountBankDto.getCondominiumId()){
             throw new BadRequestException("User cant create ");
        }
        Optional<AccountBank> accountBank =  accountBankRepository.findByCondominiumId(accountBankDto.getCondominiumId());
        if(accountBank.isPresent()){
            throw new BadRequestException("Ya existe cuenta ligada a este condominio");
        }
        AccountBank accountBankSaved =  accountBankRepository.save(accountBankMapper.convertToAccountBankModel(accountBankDto));
        return accountBankMapper.convertToAccountBankDto(accountBankSaved);
    }

    @Override
    public AccountBankDto findById(Long id) {
        return accountBankMapper.convertToAccountBankDto(accountBankRepository.findById(id).orElseThrow(()-> new BadRequestException("AccountBank not founded")));
    }

    @Override
    public List<AccountBankDto> findAll() {
         return accountBankRepository.findAll().stream().map(accountBank -> accountBankMapper.convertToAccountBankDto(accountBank)).toList();
    }

    @Override
    public AccountBankDto update(AccountBankDto accountBankDto) {
        AccountBank accountBankSaved =  accountBankRepository.save(accountBankMapper.convertToAccountBankModel(accountBankDto));
        return accountBankMapper.convertToAccountBankDto(accountBankSaved);
    }

    @Override
    public AccountBankDto findAccountBankByCondominium(Long condominiumId) {
        return accountBankMapper.convertToAccountBankDto(accountBankRepository.findByCondominiumId(condominiumId).orElseThrow(()->  new ResourceNotFoundException("No se encontro un condominio con el Id: " + condominiumId)));
    }
    @Override
    @Transactional(readOnly = true)
    public AccountBankDto findAccountBankByResident(Long residentId) {
        // Primero, busca al residente por su ID
        Resident resident = residentService.getResidentById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Residente no encontrado con el ID: " + residentId));

        // Luego, obtén el condominio del residente
        Long condominiumId = resident.getCondominium().getId();

        // Busca la cuenta bancaria asociada al condominio
        AccountBank accountBank = accountBankRepository.findByCondominiumId(condominiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta bancaria no encontrada para el condominio ID: " + condominiumId));

        // Mapear la entidad a un DTO si es necesario
        return accountBankMapper.convertToAccountBankDto(accountBank);
    }

    @Override
    public void deleteById(Long id) {
        //TODO Borrado logico o no
    }
}
