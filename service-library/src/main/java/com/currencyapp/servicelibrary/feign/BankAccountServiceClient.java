package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bank-account")
public interface BankAccountServiceClient {

    @PostMapping("/bank-accounts/internal")
    void createAccount(@RequestParam("email") String email);

    @DeleteMapping("/bank-accounts/internal")
    void deleteAccountByEmail(@RequestParam("email") String email);
}
