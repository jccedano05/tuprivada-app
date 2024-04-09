package com.jccv.tuprivadaapp.model.worker;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "workers")
public class Worker {

    @Id
    private Long id; // Esta será la misma que la clave primaria de User

    @OneToOne
    @MapsId
    @JsonBackReference
//    @JoinColumn(name = "id")
    private User user;
}
