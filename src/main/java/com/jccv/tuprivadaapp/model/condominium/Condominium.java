package com.jccv.tuprivadaapp.model.condominium;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.account_bank.AccountBank;
import com.jccv.tuprivadaapp.model.faq.Faq;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.model.notice.Notice;
import com.jccv.tuprivadaapp.model.recurring_payment.RecurringPayment;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "condominiums")
@JsonIgnoreProperties(value = {"users"})  // Ignorar la lista de usuarios para evitar recursión
public class Condominium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la privada no puede estar vacío")
    private String name;



    @OneToMany(mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    private List<User> users;

//    @OneToOne(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonManagedReference  // Este lado se serializa
    private Address address;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonBackReference  // Este lado no se serializa
    private AccountBank accountBank;

    // Relación con los residentes
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    private List<Resident> residents;

    // Posible relación con pagos recurrentes
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    private List<RecurringPayment> recurringPayments;


    // Relación con los residentes
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    private List<Contact> contacts;

    // Relación OneToMany con Notice
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Notice> notices;

    // Relación OneToMany con Notice
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Faq> Faqs;


    // Relación OneToMany con Notice
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Finance> finances;
}
