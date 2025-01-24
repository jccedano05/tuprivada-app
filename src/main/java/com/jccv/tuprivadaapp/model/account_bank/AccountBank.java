package com.jccv.tuprivadaapp.model.account_bank;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "accounts_banks")
public class AccountBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account Bank - Must contain the payment type")
    private String paymentType;

    @NotBlank(message = "Account Bank - Must contain the account name")
    private String accountName;

    @NotBlank(message = "Account Bank - Must contain the bank name")
    private String bankName;

    @NotBlank(message = "Account Bank - Must contain the number account")
    private String numberAccount;

    @NotBlank(message = "Account Bank - Must contain the clabe")
    private String clabe;

    private String reference;

    // Nuevo campo booleano
    private Boolean isGlobalReference = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", referencedColumnName = "id")
    @JsonManagedReference
    @ToString.Exclude
    private Condominium condominium;
}
