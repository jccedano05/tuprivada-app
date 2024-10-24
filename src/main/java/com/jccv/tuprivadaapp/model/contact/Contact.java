package com.jccv.tuprivadaapp.model.contact;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "phone")
    private String phone;
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "resident_id")
    @JsonBackReference
    private Resident resident;


    @ManyToOne
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    private Condominium condominium;


}
