package com.jccv.tuprivadaapp.model.condominium;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "La calle no puede estar vacío")
    private String street;
    @NotBlank(message = "El numero no puede estar vacío")
    private String number;
    @NotBlank(message = "La ciudad no puede estar vacío")
    private String city;
    @NotBlank(message = "El estado no puede estar vacío")
    private String state;

    @Digits(integer = 5, fraction = 0, message = "Formato de codigo postal invalido")
    private Integer postalCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", unique = true)
    @Valid()
    private Condominium condominium;
}
