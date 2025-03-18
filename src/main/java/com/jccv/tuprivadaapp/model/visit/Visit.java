package com.jccv.tuprivadaapp.model.visit;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jccv.tuprivadaapp.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visits")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_name")
    @NotBlank(message = "Visitor name cannot be empty")
    private String visitorName;

    @Column(name = "visitor_document")
    @NotBlank(message = "Visitor document cannot be empty")
    private String visitorDocument;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "qr_token", unique = true)
    private String qrToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VisitStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

