package com.jccv.tuprivadaapp.dto.contact;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ContactDto {
    private Long id;

    private String name;
    private String phone;
    private String description;
    private Long residentId;
    private Long condominiumId;
}
