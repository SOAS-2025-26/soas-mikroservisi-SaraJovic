package com.currencyapp.bankaccount.controller;

import com.currencyapp.bankaccount.service.BankAccountService;
import com.currencyapp.servicelibrary.dto.BankAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public List<BankAccountDto> getAllAccounts() {
        return bankAccountService.getAllAccounts();
    }

    @GetMapping("/my")
    public List<BankAccountDto> getAccountsByEmail(@RequestHeader("X-User-Email") String email) {
        return bankAccountService.getAccountsByEmail(email);
    }

    @PostMapping("/internal")
    public void createAccount(@RequestParam("email") String email) {
        bankAccountService.createAccount(email);
    }

    @DeleteMapping("/internal")
    public void deleteAccountByEmail(@RequestParam("email") String email) {
        bankAccountService.deleteAccountByEmail(email);
    }

    @PutMapping("/{id}")
    public BankAccountDto updateAccount(@PathVariable Long id, @RequestBody BankAccountDto dto) {
        return bankAccountService.updateAccount(id, dto);
    }

    @GetMapping("/balance")
    public BankAccountDto getBalance(@RequestParam("email") String email,
                                      @RequestParam("currency") String currencyCode) {
        return bankAccountService.getBalance(email, currencyCode);
    }

    @PostMapping("/deduct")
    public BankAccountDto deductAmount(@RequestParam("email") String email,
                                        @RequestParam("currency") String currencyCode,
                                        @RequestParam("amount") Double amount) {
        return bankAccountService.deductAmount(email, currencyCode, amount);
    }

    @PostMapping("/add")
    public BankAccountDto addCurrencyToAccount(@RequestParam("email") String email,
                                                 @RequestParam("currency") String currencyCode,
                                                 @RequestParam("amount") Double amount) {
        return bankAccountService.addCurrencyToAccount(email, currencyCode, amount);
    }
}
