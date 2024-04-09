package com.jccv.tuprivadaapp.model.condominium;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String number;
    private String city;
    private String state;
    private String postalCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    private Condominium condominium;
}
