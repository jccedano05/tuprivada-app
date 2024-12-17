package com.jccv.tuprivadaapp.model.recurring_payment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.recurring_payment.types.EnumPaymentFrequency;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "recurring_payments")
public class RecurringPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private int daysToDueDate;
    private String title;
    private String description;


    private LocalDate startDate;
    private LocalDate nextPaymentDate;
//    private String frequency; // Ej: "MONTHLY", "WEEKLY", etc.

    @Enumerated(EnumType.STRING)  // Asegúrate de usar EnumType.STRING para almacenar el valor como texto
    private EnumPaymentFrequency frequency;  // Cambiado de String a PaymentFrequency

    @Column(name = "is_recurring_payment_active")
    private boolean isRecurringPaymentActive = true;


//    @ManyToOne
//    @JoinColumn(name = "resident_id")
//    private Resident resident;

    @ManyToMany
    @JoinTable(
            name = "resident_recurring_payments",  // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "recurring_payment_id"),  // Columna que referencia a RecurringPayment
            inverseJoinColumns = @JoinColumn(name = "resident_id")  // Columna que referencia a Resident
    )
//@ManyToMany(mappedBy = "recurringPayments")
    @JsonBackReference
    @ToString.Exclude
private List<Resident> residents;

    @ManyToOne
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;

    // Métodos para calcular la próxima fecha de pago
    public void scheduleNextPayment() {
        switch(frequency){
            case MONTHLY :
                this.nextPaymentDate = this.nextPaymentDate.plusMonths(1);
                break;

            case YEARLY:
                this.nextPaymentDate = this.nextPaymentDate.plusYears(1);
                break;

            case WEEKLY:
                this.nextPaymentDate = this.nextPaymentDate.plusWeeks(1);
                break;
        }
//        if ("MONTHLY".equals(frequency)) {
//            this.nextPaymentDate = this.nextPaymentDate.plusMonths(1);
//        } else if ("WEEKLY".equals(frequency)) {
//            this.nextPaymentDate = this.nextPaymentDate.plusWeeks(1);
//        }

    }

    // Getters y setters
}