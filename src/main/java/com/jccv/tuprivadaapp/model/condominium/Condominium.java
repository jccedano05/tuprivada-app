package com.jccv.tuprivadaapp.model.condominium;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.account_bank.AccountBank;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.faq.Faq;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.model.notice.Notice;
import com.jccv.tuprivadaapp.model.recurring_payment.RecurringPayment;
import com.jccv.tuprivadaapp.model.contact.Contact;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.survey.Survey;
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

    private String connectedAccountId;

    @Column(name = "logo_image_name")
    private String logoImageName; // Ej: "logo_adco.jpeg"




    @OneToMany(mappedBy = "condominium")
    @ToString.Exclude
    @JsonIgnore  // Este lado se serializa
    private List<User> users;

//    @OneToOne(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonManagedReference  // Este lado se serializa
    @ToString.Exclude
    private Address address;

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference  // Este lado no se serializa
    @ToString.Exclude
    private List<AccountBank> accountBanks;

    // Relación con los residentes
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    @ToString.Exclude
    private List<Resident> residents;

    // Posible relación con pagos recurrentes
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    @ToString.Exclude
    private List<RecurringPayment> recurringPayments;


    // Relación con los residentes
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "condominium")
    @JsonIgnore  // Este lado se serializa
    @ToString.Exclude
    private List<Contact> contacts;

    // Relación OneToMany con Notice
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Notice> notices;

    // Relación OneToMany con Notice
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Faq> Faqs;


    // Relación OneToMany con Notice
    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Finance> finances;

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Charge> charges;  // Lista de cargos asociados al condominio

    @OneToMany(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Survey> surveys;


}
