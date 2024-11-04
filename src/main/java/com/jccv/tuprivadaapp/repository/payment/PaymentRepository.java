package com.jccv.tuprivadaapp.repository.payment;

import com.jccv.tuprivadaapp.dto.payment.PaymentDetailsDto;
import com.jccv.tuprivadaapp.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Buscar pagos vencidos que no han sido pagados y que aún no se les ha aplicado el recargo
    @Query("SELECT p FROM Payment p WHERE p.isPaid = false AND p.charge.dueDate < CURRENT_TIMESTAMP AND p.isPenaltyApplied = false AND p.isDeleted = false")
    List<Payment> findOverduePayments();

//    // Obtener pagos NO pagados y NO eliminados para un residente
//    Page<Payment> findByResidentIdAndIsPaidFalseAndIsDeletedFalse(Long residentId, Pageable pageable);

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
}



//package com.jccv.tuprivadaapp.repository.payment;
//
//import com.jccv.tuprivadaapp.model.payment.Payment;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface PaymentRepository extends JpaRepository<Payment, Long> {
//
//    // Buscar pagos vencidos que no han sido pagados y que aún no se les ha aplicado el recargo
//    @Query("SELECT p FROM Payment p WHERE p.isPaid = false AND p.dueDate < CURRENT_TIMESTAMP AND p.isPenaltyApplied = false AND p.isDeleted = false")
//    List<Payment> findOverduePayments();
//
//    // Método para obtener pagos no pagados y no eliminados de un residente
////    List<Payment> findByResidentAndIsPaidFalseAndIsDeletedFalse(Resident resident);
//
//    // Consulta con pageable para limitar los resultados de pagos NO pagados, NO eliminados
//    Page<Payment> findByResidentIdAndIsPaidFalseAndIsDeletedFalse(Long residentId, Pageable pageable);
//    // Consulta con pageable para limitar los resultados de pagos pagados, NO eliminados
//    Page<Payment> findByResidentIdAndIsPaidTrueAndIsDeletedFalseOrderByChargeDateDesc(Long residentId, Pageable pageable);
//
//
//    // Consulta con pageable para pagos de un residente en un rango de fechas
//    Page<Payment> findByResidentIdAndChargeDateBetweenAndIsDeletedFalseOrderByChargeDateDesc(Long residentId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
//
//    // Obtener el total de dinero que debe un residente (no pagado y no eliminado)
//    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false")
//    Double getTotalDebtForResident(Long residentId);
//
//    // Obtener la fecha de próximo vencimiento que no tenga recargo aplicado
////    @Query("SELECT p.dueDate FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false AND p.isPenaltyApplied = false AND p.dueDate >= CURRENT_TIMESTAMP ORDER BY p.dueDate ASC")
////    LocalDateTime getNextDueDateForResident(Long residentId);
////
////    @Query("SELECT p.dueDate FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false AND p.isPenaltyApplied = false AND p.dueDate < CURRENT_TIMESTAMP ORDER BY p.dueDate DESC")
////    LocalDateTime getLastDueDateForResident(Long residentId);
//
////    @Query("""
////    SELECT p.dueDate
////    FROM Payment p
////    WHERE p.resident.id = :residentId
////    AND p.isPaid = false
////    AND p.isDeleted = false
////    ORDER BY
////        CASE
////            WHEN p.dueDate < CURRENT_TIMESTAMP THEN 1
////            ELSE 0
////        END,
////        p.dueDate ASC
////""")
////    List<LocalDateTime> getRelevantDueDateForResident(Long residentId);
//
//    @Query("""
//SELECT p.dueDate
//FROM Payment p
//WHERE p.resident.id = :residentId
//AND p.isPaid = false
//AND p.isDeleted = false
//ORDER BY
//    CASE
//        WHEN p.dueDate >= CURRENT_TIMESTAMP THEN 0
//        ELSE 1
//    END,
//    p.dueDate ASC
//""")
//    List<LocalDateTime> getRelevantDueDateForResident(Long residentId);
//
//}


