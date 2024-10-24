package com.jccv.tuprivadaapp.dto.resident;


import com.jccv.tuprivadaapp.dto.contact.ContactDto;
import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentDto {

    private Long id;
    private boolean isActiveResident;
    private Long userId;  // Referencia al ID del usuario, no al objeto completo
    private AddressResidentDto addressResidentDto;
}
