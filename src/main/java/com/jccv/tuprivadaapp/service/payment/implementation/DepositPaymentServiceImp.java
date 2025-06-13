package com.jccv.tuprivadaapp.service.payment.implementation;

import com.jccv.tuprivadaapp.dto.payment.DepositPaymentDto;
import com.jccv.tuprivadaapp.dto.payment.mapper.DepositPaymentMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.payment.DepositPayment;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.receipt.Receipt;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.payment.DepositPaymentRepository;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.service.payment.DepositPaymentService;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepositPaymentServiceImp implements DepositPaymentService {

    @Autowired
    private DepositPaymentRepository depositPaymentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DepositPaymentMapper depositPaymentMapper;

    @Autowired
    private ResidentService residentService;

//    @Override
//    public DepositPaymentDTO addDeposit(Long paymentId, DepositPaymentDTO depositPaymentDTO) {
//        Payment payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
//
//
//
//        // Se crea el abono con la fecha designada o actual
//        DepositPayment depositPayment = DepositPayment.builder()
//                .amount(depositPaymentDTO.getAmount())
//                .depositDate((depositPaymentDTO.getDepositDate() != null) ? depositPaymentDTO.getDepositDate() : LocalDateTime.now())
//                .payment(payment)
//                .build();
//
//        DepositPayment savedDeposit = depositPaymentRepository.save(depositPayment);
//        return depositPaymentMapper.toDTO(savedDeposit);
//    }

    @Override
    @Transactional
    public DepositPaymentDto addDeposit(Long paymentId, DepositPaymentDto depositPaymentDTO) {
        // Buscar el Payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        // Verificar si el Charge está activo
        Charge charge = payment.getCharge();
        if (!charge.isActive()) {
            throw new BadRequestException("No se puede realizar un abono porque el cargo asociado no está activo.");
        }

        // Obtenemos el residente del Payment y validamos que tenga saldo suficiente
        Resident resident = payment.getResident();
        double depositAmount = depositPaymentDTO.getAmount();

        if (resident.getBalance() < depositAmount) {
            throw new BadRequestException("Saldo insuficiente en la cuenta del residente para realizar el depósito");
        }

        // Calculamos el total de abonos (depositos) realizados para este Payment
        List<DepositPayment> existingDeposits = depositPaymentRepository.findByPaymentId(paymentId);
        double totalDeposits = existingDeposits.stream()
                .mapToDouble(DepositPayment::getAmount)
                .sum();


        // Calculamos el saldo restante: monto total del cargo - total de abonos realizados
        double outstandingBalance = payment.getCharge().getAmount() - totalDeposits;

        // Validamos que el depósito no sea mayor que el saldo restante
        if (depositAmount >= outstandingBalance) {
            throw new BadRequestException("El depósito excede o es igual al saldo restante para liquidar el pago. Saldo restante: " + outstandingBalance);
        }
        // Se crea el abono usando la fecha enviada desde el front
        DepositPayment depositPayment = DepositPayment.builder()
                .title("Abono #" + Integer.sum(existingDeposits.size(), 1)  +" de '" + payment.getCharge().getTitleTypePayment() +  "'")
                .description("Abono #" + Integer.sum(existingDeposits.size(), 1)  +" de '" + payment.getCharge().getDescription() +  "'")
                .amount(depositPaymentDTO.getAmount())
                .depositDate(depositPaymentDTO.getDepositDate())
                .payment(payment)
                .build();


        DepositPayment savedDeposit = depositPaymentRepository.save(depositPayment);


        // Actualizamos el balance del residente restando el monto del depósito
        residentService.updateBalanceResident(resident, -depositAmount);

        return depositPaymentMapper.toDTO(savedDeposit);
    }


    @Override
    public List<DepositPaymentDto> getDepositsByPayment(Long paymentId) {
        List<DepositPayment> deposits = depositPaymentRepository.findByPaymentId(paymentId);
        return deposits.stream()
                .map(depositPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalDepositsAmountByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));


        return depositPaymentRepository.getTotalDepositsAmountByPaymentId(payment.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositPaymentDto> getDepositsByResidentAndDateRange(Long residentId, LocalDateTime startDate, LocalDateTime endDate) {

        List<DepositPayment> deposits = depositPaymentRepository.findByResidentIdAndDepositDateBetween(residentId,  startDate, endDate);

        deposits.forEach(System.out::println);
        return deposits.stream()
                .map(depositPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositPaymentDto> getDepositsByResidentId(Long residentId) {

        List<DepositPayment> deposits = depositPaymentRepository.findAllByResidentId(residentId);

        deposits.forEach(System.out::println);
        return deposits.stream()
                .map(depositPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DepositPaymentDto updateDeposit(Long depositId, DepositPaymentDto updatedDto) {
        DepositPayment existingDeposit = depositPaymentRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositPayment not found with id: " + depositId));

        double oldAmount = existingDeposit.getAmount();
        double newAmount = updatedDto.getAmount();

        if (newAmount < 0) {
            throw new BadRequestException("El nuevo monto no puede ser negativo.");
        }

        Payment payment = existingDeposit.getPayment();
        Resident resident = payment.getResident();

        // Verificamos que tenga suficiente saldo en caso de que el nuevo monto sea mayor
        double diff = newAmount - oldAmount;
        if (diff > 0 && resident.getBalance() < diff) {
            throw new BadRequestException("Saldo insuficiente para aumentar el monto del abono.");
        }


        // Actualizamos los campos permitidos
        existingDeposit.setAmount(newAmount);
        existingDeposit.setDepositDate(updatedDto.getDepositDate());

        DepositPayment saved = depositPaymentRepository.save(existingDeposit);

        residentService.updateBalanceResident(resident, -diff);

        return depositPaymentMapper.toDTO(saved);
    }


    @Transactional
    @Override
    public void deleteDeposit(Long depositId) {
        DepositPayment deposit = depositPaymentRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositPayment not found with id: " + depositId));

        Payment payment = deposit.getPayment();
        Resident resident = payment.getResident();

        // Sumamos el monto eliminado de regreso al balance del residente
        residentService.updateBalanceResident(resident, deposit.getAmount() );

        depositPaymentRepository.delete(deposit);
    }

    @Override
    @Transactional
    public void deleteAllDepositsByPaymentId(Long paymentId) {
        List<DepositPaymentDto> deposits =  getDepositsByPayment(paymentId);

        depositPaymentRepository.deleteAllById(deposits.stream().map(DepositPaymentDto::getId).toList());
    }

    @Override
    public void deleteAllDepositsWithBalanceUpdateByPaymentId(Long paymentId, Resident resident) {
        List<DepositPaymentDto> deposits =  getDepositsByPayment(paymentId);

        double depositsTotalAmount = 0;

        depositsTotalAmount = deposits.stream()
                .mapToDouble(DepositPaymentDto::getAmount)
                .sum();

        residentService.updateBalanceResident(resident, depositsTotalAmount);
        depositPaymentRepository.deleteAllById(deposits.stream().map(DepositPaymentDto::getId).toList());

    }

    @Override
    public Receipt generateDepositPaymentReceiptData(Long id) {
        DepositPayment payment = depositPaymentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Deposit Payment no encontrado con el id: " + id));

        String residentAddress = payment.getPayment().getResident().getAddressResident().getStreet().concat(" #").concat(payment.getPayment().getResident().getAddressResident().getExtNumber());
        String residentName = payment.getPayment().getResident().getUser().getFirstName().concat(" ").concat(payment.getPayment().getResident().getUser().getLastName());

        return  Receipt.builder()
                .depositPayment(payment)
                .receiptName("Abono a cargo")
                .createdAt(LocalDateTime.now())
                .condominium(payment.getPayment().getCharge().getCondominium())
                .amount(payment.getAmount())
                .residentAddress(residentAddress)
                .residentName(residentName)
                .datePaid(payment.getDepositDate())
                .title("Abono - ".concat(payment.getPayment().getCharge().getTitleTypePayment()))
                .description("Abono - ".concat(payment.getPayment().getCharge().getDescription()))
                .build();
    }


}
