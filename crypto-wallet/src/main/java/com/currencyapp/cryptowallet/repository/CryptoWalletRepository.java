package com.currencyapp.cryptowallet.repository;

import com.currencyapp.cryptowallet.entity.CryptoWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {

    List<CryptoWallet> findByEmail(String email);

    void deleteByEmail(String email);

    Optional<CryptoWallet> findByEmailAndCurrencyCode(String email, String currencyCode);
}
