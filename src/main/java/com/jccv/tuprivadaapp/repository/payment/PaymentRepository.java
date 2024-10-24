package com.jccv.tuprivadaapp.repository.payment;

import com.jccv.tuprivadaapp.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Buscar pagos vencidos que no han sido pagados y que aún no se les ha aplicado el recargo
    @Query("SELECT p FROM Payment p WHERE p.isPaid = false AND p.dueDate < CURRENT_TIMESTAMP AND p.isPenaltyApplied = false AND p.isDeleted = false")
    List<Payment> findOverduePayments();

    // Método para obtener pagos no pagados y no eliminados de un residente
//    List<Payment> findByResidentAndIsPaidFalseAndIsDeletedFalse(Resident resident);

    // Consulta con pageable para limitar los resultados de pagos NO pagados, NO eliminados
    Page<Payment> findByResidentIdAndIsPaidFalseAndIsDeletedFalse(Long residentId, Pageable pageable);
    // Consulta con pageable para limitar los resultados de pagos pagados, NO eliminados
    Page<Payment> findByResidentIdAndIsPaidTrueAndIsDeletedFalseOrderByChargeDateDesc(Long residentId, Pageable pageable);


    // Consulta con pageable para pagos de un residente en un rango de fechas
    Page<Payment> findByResidentIdAndChargeDateBetweenAndIsDeletedFalseOrderByChargeDateDesc(Long residentId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Obtener el total de dinero que debe un residente (no pagado y no eliminado)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false")
    Double getTotalDebtForResident(Long residentId);

    // Obtener la fecha de próximo vencimiento que no tenga recargo aplicado
//    @Query("SELECT p.dueDate FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false AND p.isPenaltyApplied = false AND p.dueDate >= CURRENT_TIMESTAMP ORDER BY p.dueDate ASC")
//    LocalDateTime getNextDueDateForResident(Long residentId);
//
//    @Query("SELECT p.dueDate FROM Payment p WHERE p.resident.id = :residentId AND p.isPaid = false AND p.isDeleted = false AND p.isPenaltyApplied = false AND p.dueDate < CURRENT_TIMESTAMP ORDER BY p.dueDate DESC")
//    LocalDateTime getLastDueDateForResident(Long residentId);

//    @Query("""
//    SELECT p.dueDate
//    FROM Payment p
//    WHERE p.resident.id = :residentId
//    AND p.isPaid = false
//    AND p.isDeleted = false
//    ORDER BY
//        CASE
//            WHEN p.dueDate < CURRENT_TIMESTAMP THEN 1
//            ELSE 0
//        END,
//        p.dueDate ASC
//""")
//    List<LocalDateTime> getRelevantDueDateForResident(Long residentId);

    @Query("""
SELECT p.dueDate
FROM Payment p
WHERE p.resident.id = :residentId
AND p.isPaid = false
AND p.isDeleted = false
ORDER BY
    CASE
        WHEN p.dueDate >= CURRENT_TIMESTAMP THEN 0  
        ELSE 1                                       
    END,
    p.dueDate ASC  
""")
    List<LocalDateTime> getRelevantDueDateForResident(Long residentId);

}
