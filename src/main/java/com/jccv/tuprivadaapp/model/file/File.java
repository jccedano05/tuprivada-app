package com.jccv.tuprivadaapp.model.file;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del archivo no puede estar vacío")
    private String fileName;

    @NotBlank(message = "El tipo de archivo no puede estar vacío")
    private String fileType;


    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
