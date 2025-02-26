package com.jccv.tuprivadaapp.model.resident;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.recurring_payment.RecurringPayment;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "residents", indexes = {
        @Index(name = "idx_resident_condominium", columnList = "condominium_id")
})
public class Resident  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_active_resident")
    private boolean isActiveResident = true;


    @Column(name = "balance", nullable = false)
    private Double balance = 0.0;




    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToOne(mappedBy = "resident",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true) // Agregar cascade y orphanRemoval si aplica
//    @JsonManagedReference  // Este lado se serializa
    @ToString.Exclude
    private AddressResident addressResident;


    @JsonIgnore
    @OneToMany(mappedBy = "resident", cascade = CascadeType.MERGE,fetch = FetchType.LAZY)
    private List<Contact> contacts;


    @ManyToOne
    @JoinColumn(name = "condominium_id")
    @ToString.Exclude
    private Condominium condominium;


    @ManyToMany(mappedBy = "residents")  // La relación inversa, mapea a la lista "residents" de RecurringPayment
    @JsonManagedReference  // Este lado se serializa
    @ToString.Exclude
    private List<RecurringPayment> recurringPayments;

    @OneToMany(mappedBy = "resident", cascade = CascadeType.MERGE,fetch = FetchType.LAZY)
    @JsonManagedReference  // Este lado se serializa
    @ToString.Exclude
    private List<Payment> payments;





}
