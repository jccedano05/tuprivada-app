package com.jccv.tuprivadaapp.model.account_bank;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @NotBlank(message = "Account Bank - Must cointain the account name ")
    private String accountName;
    @NotBlank(message = "Account Bank - Must cointain the bank name ")
    private String bankName;
    @NotBlank(message = "Account Bank - Must cointain the number account  ")
    private String numberAccount;
    @NotBlank(message = "Account Bank - Must cointain the clabe ")
    private String clabe;
    @NotBlank(message = "Account Bank - Must cointain the reference ")
    private String reference;



    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", referencedColumnName = "id", unique = true)
    @JsonManagedReference  // Este lado se serializa
    private Condominium condominium;
}
