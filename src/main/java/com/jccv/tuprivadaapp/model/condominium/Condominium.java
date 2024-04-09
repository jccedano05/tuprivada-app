package com.jccv.tuprivadaapp.model.condominium;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @OneToOne(mappedBy = "condominium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Address address;

}
