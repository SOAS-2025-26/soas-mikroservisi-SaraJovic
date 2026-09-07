package com.currencyapp.servicelibrary.feign;

import com.currencyapp.servicelibrary.dto.BankAccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bank-account", configuration = InternalAuthFeignConfig.class)
public interface BankAccountServiceClient {

    @PostMapping("/bank-accounts/internal")
    void createAccount(@RequestParam("email") String email);

    @DeleteMapping("/bank-accounts/internal")
    void deleteAccountByEmail(@RequestParam("email") String email);

    @GetMapping("/bank-accounts/balance")
    BankAccountDto getBalance(@RequestParam("email") String email, @RequestParam("currency") String currency);

    @PostMapping("/bank-accounts/deduct")
    BankAccountDto deductAmount(@RequestParam("email") String email,
                                 @RequestParam("currency") String currency,
                                 @RequestParam("amount") Double amount);

    @PostMapping("/bank-accounts/add")
    BankAccountDto addCurrencyToAccount(@RequestParam("email") String email,
                                         @RequestParam("currency") String currency,
                                         @RequestParam("amount") Double amount);
}
