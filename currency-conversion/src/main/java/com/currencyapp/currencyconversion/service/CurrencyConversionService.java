package com.currencyapp.currencyconversion.service;

import com.currencyapp.servicelibrary.dto.BankAccountDto;
import com.currencyapp.servicelibrary.dto.ConversionResultDto;
import com.currencyapp.servicelibrary.dto.ExchangeRateDto;
import com.currencyapp.servicelibrary.feign.BankAccountServiceClient;
import com.currencyapp.servicelibrary.feign.CurrencyExchangeServiceClient;
import com.currencyapp.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final CurrencyExchangeServiceClient currencyExchangeServiceClient;
    private final BankAccountServiceClient bankAccountServiceClient;

    public ConversionResultDto convert(String from, String to, Double quantity, String userEmail) {
        ExchangeRateDto exchangeRate = currencyExchangeServiceClient.getExchangeRate(from, to);

        BankAccountDto sourceAccount = bankAccountServiceClient.getBalance(userEmail, from);

        if (sourceAccount.getAmount() < quantity) {
            throw new BusinessException("Insufficient funds", HttpStatus.BAD_REQUEST);
        }

        Double convertedAmount = quantity * exchangeRate.getRate();

        bankAccountServiceClient.deductAmount(userEmail, from, quantity);
        BankAccountDto updatedAccount = bankAccountServiceClient.addCurrencyToAccount(userEmail, to, convertedAmount);

        String transactionMessage = "Successfully exchanged " + from + ": " + quantity + " for " + to + ": " + convertedAmount;

        return ConversionResultDto.builder()
                .bankAccount(updatedAccount)
                .transactionMessage(transactionMessage)
                .build();
    }
}
