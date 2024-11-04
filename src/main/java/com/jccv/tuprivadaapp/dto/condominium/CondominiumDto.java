package com.jccv.tuprivadaapp.dto.condominium;

import lombok.*;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Component
public class CondominiumDto {

    private Long id;

    private String name;

    private Long addressId;


}
