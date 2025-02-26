package com.jccv.tuprivadaapp.service.transaction.implementation;


import com.jccv.tuprivadaapp.controller.pushNotifications.PushNotificationRequest;
import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.dto.transaction.*;
import com.jccv.tuprivadaapp.dto.transaction.mapper.DepositMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.transaction.Deposit;
import com.jccv.tuprivadaapp.repository.transaction.DepositRepository;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import com.jccv.tuprivadaapp.service.pushNotifications.OneSignalPushNotificationService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import com.jccv.tuprivadaapp.service.transaction.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepositServiceImp implements DepositService {

    private final DepositRepository depositRepository;
    private final ResidentService residentService;
    private final DepositMapper depositMapper;
    private final PollingNotificationService pollingNotificationService;
    private final OneSignalPushNotificationService oneSignalPushNotificationService;


    @Autowired
    public DepositServiceImp(DepositRepository depositRepository, ResidentService residentService, DepositMapper depositMapper, PollingNotificationService pollingNotificationService, OneSignalPushNotificationService oneSignalPushNotificationService) {
        this.depositRepository = depositRepository;
        this.residentService = residentService;
        this.depositMapper = depositMapper;
        this.pollingNotificationService = pollingNotificationService;
        this.oneSignalPushNotificationService = oneSignalPushNotificationService;
    }

    @Transactional
    @Override
    public DepositDto createDeposit(DepositDto depositDTO) {
        // Obtener el residente
        Resident resident = residentService.getResidentById(depositDTO.getResidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Residente no encontrado"));

        // Crear el depósito
        Deposit deposit = depositMapper.convertToEntity(depositDTO, resident);
        deposit.setResident(resident);

        // Actualizar el saldo del residente
        double newBalance = resident.getBalance() + depositDTO.getAmount();
        resident.setBalance(newBalance);

        // Guardar el depósito y el residente actualizado
        residentService.saveResident(resident);
        deposit.setBalanceAfterDeposit(newBalance);

        Deposit depositSaved = depositRepository.save(deposit);

        pollingNotificationService.createNotification(PollingNotificationDto.builder()
                .title("Deposito agregado exitosamente.!")
                .message("Se ha agregado el deposito de "+deposit.getAmount() + "a su cuenta.")
                .userId(resident.getUser().getId())
                .read(false)
                .build());
        oneSignalPushNotificationService.sendPushToUser(PushNotificationRequest.builder()
                .title("Deposito agregado.!")
                .message("Se ha agregado el deposito de "+deposit.getAmount() + "a su cuenta.")
                .userId(resident.getUser().getId())
                .build());

        return depositMapper.convertToDto(depositSaved);
    }

    @Transactional
    @Override
    public DepositDto updateDeposit(Long id, DepositDto depositDTO) {
        // Obtener el depósito existente
        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado"));

        // Actualizar el saldo anterior del residente antes de modificar el depósito
        Resident resident = deposit.getResident();
        double previousBalance = resident.getBalance();
        double newBalance = previousBalance - deposit.getAmount() + depositDTO.getAmount(); // Nuevo saldo

        // Actualizar el depósito
        deposit.setAmount(depositDTO.getAmount());
        deposit.setDepositDate(depositDTO.getDepositDate());
        deposit.setBankTrackingKey(depositDTO.getBankTrackingKey());
        deposit.setIssuingBank(depositDTO.getIssuingBank());

        // Actualizar el saldo del residente
        resident.setBalance(newBalance);
        residentService.saveResident(resident);

        // Guardar el depósito actualizado
        deposit.setBalanceAfterDeposit(newBalance);
        return depositMapper.convertToDto(depositRepository.save(deposit));
    }

    @Transactional
    @Override
    public boolean deleteDeposit(Long id) {
        // Obtener el depósito
        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado"));

        // Actualizar el saldo del residente (restando el monto del depósito)
        Resident resident = deposit.getResident();
        double newBalance = resident.getBalance() - deposit.getAmount();
        resident.setBalance(newBalance);
        residentService.saveResident(resident);

        // Eliminar el depósito
        depositRepository.delete(deposit);
        return true;
    }

    @Override
    public Deposit findById(Long id) {
        return depositRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Deposit not found with id: " + id));
    }

    @Override
    public List<DepositDto> findDepositsByResidentId(Long residentId) {
        List<Deposit> deposits = depositRepository.findAllByResidentId(residentId);
        return deposits.stream().map(depositMapper::convertToDto).toList();
    }



    @Override
    public List<DepositDto> getDepositsByCondominiumIdAndMonth(Long condominiumId, int month, int year) {
        List<Deposit> deposits = depositRepository.findByCondominiumIdAndMonth(condominiumId, month, year);
        if (month < 1 || month > 12) {
            throw new BadRequestException("Mes inválido. Debe estar entre 1 y 12.");
        }
        return deposits.stream().map(depositMapper::convertToDto).toList();
    }

    @Override
    public List<DepositDto> getDepositsByCondominiumIdAndYear(Long condominiumId, int year) {
        List<Deposit> deposits = depositRepository.findByCondominiumIdAndYear(condominiumId, year);
        return deposits.stream().map(depositMapper::convertToDto).toList();
    }

    @Override
    public DepositSummaryDto getDepositSummaryForMonthAndYear(Long condominiumId, int month, int year) {
        // Obtener depósitos del condominio para el mes actual
        List<Deposit> depositsInMonth = depositRepository.findByCondominiumIdAndMonth(condominiumId, month, year);
        List<Deposit> depositsInYear = depositRepository.findByCondominiumIdAndYear(condominiumId, year);

        // Sumar los montos de los depósitos del mes
        double amountInMonth = depositsInMonth.stream()
                .mapToDouble(Deposit::getAmount)
                .sum();

        // Sumar los montos de los depósitos del año
        double amountInYear = depositsInYear.stream()
                .mapToDouble(Deposit::getAmount)
                .sum();

        // Devolver el DTO con los totales
        return new DepositSummaryDto(amountInMonth, amountInYear);
    }

    @Override
    public AnnualDepositSummaryDto getAnnualDepositSummary(Long condominiumId, int year) {
        // 1. Obtener breakdown mensual
        List<Object[]> monthlyData = depositRepository.getMonthlyBreakdown(condominiumId, year);

        // 2. Calcular totales anuales
        double totalAmountYear = 0;
        int totalDeposits = 0;
        List<MonthlyDepositDto> monthlyBreakdown = new ArrayList<>();

        for (Object[] data : monthlyData) {
            int month = (int) data[0];
            double amount = (double) data[2];
            long deposits = (long) data[3];
            long residents = (long) data[4];

            totalAmountYear += amount;
            totalDeposits += deposits;

            monthlyBreakdown.add(MonthlyDepositDto.builder()
                    .month(getSpanishMonthName(month))
                    .amount(amount)
                    .depositCount((int) deposits)
                    .residentCount((int) residents)
                    .build());
        }

        // 3. Obtener residentes únicos
        int totalResidents = depositRepository.countUniqueResidents(condominiumId, year);

        // 4. Obtener depósitos recientes
        List<Deposit> recentDeposits = depositRepository.findRecentDeposits(
                condominiumId,
                year,
               5
        );

        List<RecentDepositDto> recentDepositDtos = recentDeposits.stream()
                .map(d -> RecentDepositDto.builder()
                        .id(d.getId())
                        .residentName(d.getResident().getUser().getFirstName() + " " + d.getResident().getUser().getLastName())
                        .amount(d.getAmount())
                        .date(d.getDepositDate().toLocalDate().toString())
                        .build())
                .collect(Collectors.toList());

        return AnnualDepositSummaryDto.builder()
                .totalAmountYear(totalAmountYear)
                .totalDeposits(totalDeposits)
                .totalResidents(totalResidents)
                .monthlyBreakdown(monthlyBreakdown)
                .recentDeposits(recentDepositDtos)
                .build();
    }

    private String getSpanishMonthName(int month) {
        return new String[]{"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"}[month-1];
    }

}
