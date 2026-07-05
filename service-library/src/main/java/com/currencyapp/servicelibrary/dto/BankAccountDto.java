package com.currencyapp.servicelibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountDto {

    private Long id;
    private String email;
    private String currencyCode;
    private Double amount;
}
