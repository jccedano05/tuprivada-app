package com.jccv.tuprivadaapp.dto.acoountBank;


import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AccountBankDto {
    private Long id;

    private String accountName;
    private String bankName;
    private String numberAccount;
    private String clabe;
    private String reference;

    private Long condominiumId;


}