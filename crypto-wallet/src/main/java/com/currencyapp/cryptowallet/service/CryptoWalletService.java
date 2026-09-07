package com.currencyapp.cryptowallet.service;

import com.currencyapp.cryptowallet.entity.CryptoWallet;
import com.currencyapp.cryptowallet.repository.CryptoWalletRepository;
import com.currencyapp.servicelibrary.dto.CryptoWalletDto;
import com.currencyapp.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptoWalletService {

    private static final String DEFAULT_CURRENCY = "ETH";

    private final CryptoWalletRepository cryptoWalletRepository;

    public List<CryptoWalletDto> getAllWallets() {
        return cryptoWalletRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<CryptoWalletDto> getWalletsByEmail(String email) {
        return cryptoWalletRepository.findByEmail(email).stream()
                .map(this::toDto)
                .toList();
    }

    public void createWallet(String email) {
        if (cryptoWalletRepository.existsByEmailAndCurrencyCode(email, DEFAULT_CURRENCY)) {
            return;
        }

        CryptoWallet wallet = CryptoWallet.builder()
                .email(email)
                .currencyCode(DEFAULT_CURRENCY)
                .amount(0.0)
                .build();

        cryptoWalletRepository.save(wallet);
    }

    public CryptoWalletDto addCryptoToWallet(String email, String currencyCode, Double amount) {
        CryptoWallet wallet = cryptoWalletRepository.findByEmailAndCurrencyCode(email, currencyCode)
                .orElseGet(() -> CryptoWallet.builder()
                        .email(email)
                        .currencyCode(currencyCode)
                        .amount(0.0)
                        .build());

        wallet.setAmount(wallet.getAmount() + amount);

        return toDto(cryptoWalletRepository.save(wallet));
    }

    public CryptoWalletDto updateWallet(Long id, CryptoWalletDto dto) {
        CryptoWallet wallet = cryptoWalletRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Crypto wallet not found with id " + id, HttpStatus.NOT_FOUND));

        wallet.setEmail(dto.getEmail());
        wallet.setCurrencyCode(dto.getCurrencyCode());
        wallet.setAmount(dto.getAmount());

        return toDto(cryptoWalletRepository.save(wallet));
    }

    @Transactional
    public void deleteWalletByEmail(String email) {
        cryptoWalletRepository.deleteByEmail(email);
    }

    public CryptoWalletDto deductAmount(String email, String currencyCode, Double amount) {
        CryptoWallet wallet = cryptoWalletRepository.findByEmailAndCurrencyCode(email, currencyCode)
                .orElseThrow(() -> new BusinessException("Crypto wallet not found for " + email + " in " + currencyCode, HttpStatus.NOT_FOUND));

        if (wallet.getAmount() < amount) {
            throw new BusinessException("Insufficient funds", HttpStatus.BAD_REQUEST);
        }

        wallet.setAmount(wallet.getAmount() - amount);

        return toDto(cryptoWalletRepository.save(wallet));
    }

    public CryptoWalletDto getBalance(String email, String currencyCode) {
        CryptoWallet wallet = cryptoWalletRepository.findByEmailAndCurrencyCode(email, currencyCode)
                .orElseThrow(() -> new BusinessException("Crypto wallet not found for " + email + " in " + currencyCode, HttpStatus.NOT_FOUND));

        return toDto(wallet);
    }

    private CryptoWalletDto toDto(CryptoWallet wallet) {
        return CryptoWalletDto.builder()
                .id(wallet.getId())
                .email(wallet.getEmail())
                .currencyCode(wallet.getCurrencyCode())
                .amount(wallet.getAmount())
                .build();
    }
}
