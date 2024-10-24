package com.jccv.tuprivadaapp.dto.screens.mobile;


import com.jccv.tuprivadaapp.dto.screens.mobile.payments.ResidentAccountPaymentDetailsDto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PaymentsScreenDto {

    private ResidentAccountPaymentDetailsDto residentAccountPaymentDetailsDto;

}
