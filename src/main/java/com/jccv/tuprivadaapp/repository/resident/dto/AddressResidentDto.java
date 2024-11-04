package com.jccv.tuprivadaapp.repository.resident.dto;

import com.jccv.tuprivadaapp.model.resident.AddressResident;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.resident.facade.ResidentFacade;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AddressResidentDto {

    private Long id;

    @NotBlank(message = "La calle no puede quedar vacia")
    private String street;


    @NotBlank(message = "El numero no puede quedar vacio")
    private String extNumber;
    private String intNumber;



    private String intercom;  //interfon

    @DecimalMin(value = "0" ,message = "El id del residente no debe estar vacío")
    private Long residentId;




}
