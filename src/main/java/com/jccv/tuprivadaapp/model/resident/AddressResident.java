package com.jccv.tuprivadaapp.model.resident;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "addresses_residents")
public class AddressResident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La calle no puede quedar vacia")
    private String street;


    @NotBlank(message = "El numero no puede quedar vacio")
    private String extNumber;

    private String intNumber;

    private String intercom;  //interfon

    @OneToOne
    @JoinColumn(name = "resident_id", nullable = false)
    @NotNull(message = "El residente no debe estar vacío")
    @JsonBackReference  // Evita recursión infinita al serializar
    @ToString.Exclude
    private Resident resident;

}
