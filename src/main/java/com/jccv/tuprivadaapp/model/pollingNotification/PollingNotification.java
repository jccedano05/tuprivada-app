package com.jccv.tuprivadaapp.model.pollingNotification;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "polling_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollingNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String message;
    private boolean read = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    @ToString.Exclude
    private User user;

//    @ManyToOne
//    @JoinColumn(name = "condominium_id")
//    @JsonBackReference
//    @ToString.Exclude
//    private Condominium condominium; // opcional si es una notificación general

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters...
}
