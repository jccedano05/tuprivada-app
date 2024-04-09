package com.jccv.tuprivadaapp.model.resident;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "residents")
//@PrimaryKeyJoinColumn(name = "id") // Utilizamos una columna de clave primaria para la herencia
public class Resident extends User {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;

    @JsonManagedReference
    @OneToMany(mappedBy = "resident", cascade = CascadeType.MERGE,fetch = FetchType.LAZY)
    private List<Contact> contacts;
}
