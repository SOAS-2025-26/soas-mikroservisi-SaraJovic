package com.currencyapp.bankaccount.repository;

import com.currencyapp.bankaccount.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByEmail(String email);

    void deleteByEmail(String email);

    Optional<BankAccount> findByEmailAndCurrencyCode(String email, String currencyCode);
}
