package com.currencyapp.bankaccount.service;

import com.currencyapp.bankaccount.entity.BankAccount;
import com.currencyapp.bankaccount.repository.BankAccountRepository;
import com.currencyapp.servicelibrary.dto.BankAccountDto;
import com.currencyapp.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private static final String DEFAULT_CURRENCY = "EUR";

    private final BankAccountRepository bankAccountRepository;

    public List<BankAccountDto> getAllAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<BankAccountDto> getAccountsByEmail(String email) {
        return bankAccountRepository.findByEmail(email).stream()
                .map(this::toDto)
                .toList();
    }

    public void createAccount(String email) {
        if (bankAccountRepository.existsByEmailAndCurrencyCode(email, DEFAULT_CURRENCY)) {
            return;
        }

        BankAccount account = BankAccount.builder()
                .email(email)
                .currencyCode(DEFAULT_CURRENCY)
                .amount(0.0)
                .build();

        bankAccountRepository.save(account);
    }

    public BankAccountDto addCurrencyToAccount(String email, String currencyCode, Double amount) {
        BankAccount account = bankAccountRepository.findByEmailAndCurrencyCode(email, currencyCode)
                .orElseGet(() -> BankAccount.builder()
                        .email(email)
                        .currencyCode(currencyCode)
                        .amount(0.0)
                        .build());

        account.setAmount(account.getAmount() + amount);

        return toDto(bankAccountRepository.save(account));
    }

    public BankAccountDto updateAccount(Long id, BankAccountDto dto) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Bank account not found with id " + id, HttpStatus.NOT_FOUND));

        account.setEmail(dto.getEmail());
        account.setCurrencyCode(dto.getCurrencyCode());
        account.setAmount(dto.getAmount());

        return toDto(bankAccountRepository.save(account));
    }

    @Transactional
    public void deleteAccountByEmail(String email) {
        bankAccountRepository.deleteByEmail(email);
    }

    public BankAccountDto deductAmount(String email, String currencyCode, Double amount) {
        BankAccount account = bankAccountRepository.findByEmailAndCurrencyCode(email, currencyCode)
                .orElseThrow(() -> new BusinessException("Bank account not found for " + email + " in " + currencyCode, HttpStatus.NOT_FOUND));

        if (account.getAmount() < amount) {
            throw new BusinessException("Insufficient funds", HttpStatus.BAD_REQUEST);
        }

        account.setAmount(account.getAmount() - amount);

        return toDto(bankAccountRepository.save(account));
    }

    public BankAccountDto getBalance(String email, String currencyCode) {
        BankAccount account = bankAccountRepository.findByEmailAndCurrencyCode(email, currencyCode)
                .orElseThrow(() -> new BusinessException("Bank account not found for " + email + " in " + currencyCode, HttpStatus.NOT_FOUND));

        return toDto(account);
    }

    private BankAccountDto toDto(BankAccount account) {
        return BankAccountDto.builder()
                .id(account.getId())
                .email(account.getEmail())
                .currencyCode(account.getCurrencyCode())
                .amount(account.getAmount())
                .build();
    }
}
