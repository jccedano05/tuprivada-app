package com.jccv.tuprivadaapp.model.condominium;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jccv.tuprivadaapp.model.User;
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
public class Condominium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "El nombre de la privada no puede estar vacío")
    private String name;

    @OneToMany(mappedBy = "condominium")
    private List<User> users;

    @OneToOne(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Address address;

}
