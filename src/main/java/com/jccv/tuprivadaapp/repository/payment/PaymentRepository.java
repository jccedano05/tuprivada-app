package com.jccv.tuprivadaapp.repository.payment;

import com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto;
import com.jccv.tuprivadaapp.dto.payment.PaymentResidentDetailsDto;
import com.jccv.tuprivadaapp.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByResidentIdAndChargeId(Long residentId, Long chargeId);


    // Buscar pagos vencidos que no han sido pagados y que aún no se les ha aplicado el recargo
    @Query("SELECT p FROM Payment p WHERE p.isPaid = false AND p.charge.dueDate < CURRENT_TIMESTAMP AND p.isPenaltyApplied = false AND p.isDeleted = false")
    List<Payment> findOverduePayments();



    @Query("SELECT new com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto(" +
            "p.id, p.isPaid, p.isDeleted, c.chargeDate, c.titleTypePayment, c.amount, c.description, c.isActive, c.dueDate, c.penaltyValue) " +
            "FROM Payment p JOIN p.charge c " +
            "WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false " +
            "ORDER BY c.chargeDate DESC")
    Page<PaymentDetailsDto> findByResidentIdAndIsPaidFalseAndIsDeletedFalse(@Param("residentId") Long residentId, Pageable pageable);

    @Query("SELECT new com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto(" +
            "p.id, p.isPaid, p.isDeleted, c.chargeDate, c.titleTypePayment, c.amount, c.description, c.isActive, c.dueDate, c.penaltyValue) " +
            "FROM Payment p JOIN p.charge c " +
            "WHERE p.resident.id = :residentId AND p.isPaid = true AND p.isDeleted = false " +
            "ORDER BY c.chargeDate DESC")
    Page<PaymentDetailsDto> findByResidentIdAndIsPaidTrueAndIsDeletedFalse(@Param("residentId") Long residentId, Pageable pageable);

    @Query("SELECT new com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto(" +
            "p.id, p.isPaid, p.isDeleted, c.chargeDate, c.titleTypePayment, c.amount, c.description, c.isActive, c.dueDate, c.penaltyValue) " +
            "FROM Payment p JOIN p.charge c " +
            "WHERE p.resident.id = :residentId AND c.chargeDate BETWEEN :startDate AND :endDate AND p.isDeleted = false " +
            "ORDER BY c.chargeDate DESC")
    Page<PaymentDetailsDto> findByResidentIdAndChargeDateBetweenAndIsDeletedFalse(
            @Param("residentId") Long residentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

//    // Obtener pagos pagados y NO eliminados ordenados por la fecha del cargo
//    Page<Payment> findByResidentIdAndIsPaidTrueAndIsDeletedFalseOrderByCharge_ChargeDateDesc(Long residentId, Pageable pageable);
//
//    // Obtener pagos de un residente dentro de un rango de fechas según la fecha de cargo
//    Page<Payment> findByResidentIdAndCharge_ChargeDateBetweenAndIsDeletedFalseOrderByCharge_ChargeDateDesc(Long residentId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Obtener el total de dinero que debe un residente (no pagado y no eliminado)
    @Query("SELECT SUM(p.charge.amount) FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false")
    Double getTotalDebtForResident(Long residentId);

    // Obtener la fecha de próximo vencimiento de pagos que no tengan recargo aplicado
    @Query("""
        SELECT p.charge.dueDate FROM Payment p
        WHERE p.resident.id = :residentId 
        AND p.isPaid = false 
        AND p.isDeleted = false 
        AND p.isPenaltyApplied = false 
        AND p.charge.dueDate >= CURRENT_TIMESTAMP 
        ORDER BY p.charge.dueDate ASC
    """)
    LocalDateTime getNextDueDateForResident(Long residentId);

    // Obtener la fecha de último vencimiento (más antigua) que no tenga recargo aplicado
    @Query("""
        SELECT p.charge.dueDate FROM Payment p
        WHERE p.resident.id = :residentId 
        AND p.isPaid = false 
        AND p.isDeleted = false 
        AND p.isPenaltyApplied = false 
        AND p.charge.dueDate < CURRENT_TIMESTAMP 
        ORDER BY p.charge.dueDate DESC
    """)
    LocalDateTime getLastDueDateForResident(Long residentId);

    // Obtener fechas de vencimiento relevantes para un residente
    @Query("""
        SELECT p.charge.dueDate FROM Payment p
        WHERE p.resident.id = :residentId 
        AND p.isPaid = false 
        AND p.isDeleted = false 
        ORDER BY
            CASE WHEN p.charge.dueDate >= CURRENT_TIMESTAMP THEN 0 ELSE 1 END,
            p.charge.dueDate ASC
    """)
    List<LocalDateTime> getRelevantDueDateForResident(Long residentId);

    // Query para obtener pagos por chargeId
    @Query("SELECT new com.jccv.tuprivadaapp.dto.payment.PaymentResidentDetailsDto( " +
            "p.id, r.id, c.id, r.user.firstName, r.user.lastName, p.isPaid, p.charge.amount, p.charge.description, " +
            "r.addressResident.street, r.addressResident.extNumber, r.addressResident.intNumber " +
            ") " +
            "FROM Payment p " +
            "JOIN p.resident r " +
            "JOIN r.user u " +
            "JOIN p.charge c " +
            "WHERE c.id = :chargeId")
    List<PaymentResidentDetailsDto> findAllByChargeId(Long chargeId);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.isDeleted = true WHERE p.charge.id = :chargeId")
    int markPaymentsAsDeletedByChargeId(@Param("chargeId") Long chargeId);


}


