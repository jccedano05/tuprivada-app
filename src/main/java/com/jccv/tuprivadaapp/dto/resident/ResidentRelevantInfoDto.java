package com.jccv.tuprivadaapp.dto.resident;

import com.jccv.tuprivadaapp.dto.auth.UserDto;
import com.jccv.tuprivadaapp.repository.resident.dto.AddressResidentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentRelevantInfoDto {
    private Long id;
    private boolean isActiveResident;
    private String bankPersonalReference;
    private UserDto userDto;  // Referencia al ID del usuario, no al objeto completo
    private AddressResidentDto addressResidentDto;
    private Double balance;
}
