package com.currencyapp.servicelibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversionResultDto {

    private BankAccountDto bankAccount;
    private String transactionMessage;
    private Double convertedAmount;
}
