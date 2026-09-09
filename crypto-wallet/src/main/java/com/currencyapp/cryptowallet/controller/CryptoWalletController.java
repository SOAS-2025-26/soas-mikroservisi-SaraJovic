package com.currencyapp.cryptowallet.controller;

import com.currencyapp.cryptowallet.service.CryptoWalletService;
import com.currencyapp.servicelibrary.dto.CryptoWalletDto;
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
@RequestMapping("/crypto-wallets")
@RequiredArgsConstructor
public class CryptoWalletController {

    private final CryptoWalletService cryptoWalletService;

    @GetMapping
    public List<CryptoWalletDto> getAllWallets() {
        return cryptoWalletService.getAllWallets();
    }

    @GetMapping("/my")
    public List<CryptoWalletDto> getWalletsByEmail(@RequestHeader("X-User-Email") String email) {
        return cryptoWalletService.getWalletsByEmail(email);
    }

    @PostMapping("/internal")
    public void createWallet(@RequestParam("email") String email) {
        cryptoWalletService.createWallet(email);
    }

    @DeleteMapping("/internal")
    public void deleteWalletByEmail(@RequestParam("email") String email) {
        cryptoWalletService.deleteWalletByEmail(email);
    }

    @PutMapping("/{id}")
    public CryptoWalletDto updateWallet(@PathVariable Long id, @RequestBody CryptoWalletDto dto) {
        return cryptoWalletService.updateWallet(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteWallet(@PathVariable Long id) {
        cryptoWalletService.deleteWallet(id);
    }

    @GetMapping("/balance")
    public CryptoWalletDto getBalance(@RequestParam("email") String email,
                                       @RequestParam("currency") String currencyCode) {
        return cryptoWalletService.getBalance(email, currencyCode);
    }

    @PostMapping("/deduct")
    public CryptoWalletDto deductAmount(@RequestParam("email") String email,
                                         @RequestParam("currency") String currencyCode,
                                         @RequestParam("amount") Double amount) {
        return cryptoWalletService.deductAmount(email, currencyCode, amount);
    }

    @PostMapping("/add")
    public CryptoWalletDto addCryptoToWallet(@RequestParam("email") String email,
                                              @RequestParam("currency") String currencyCode,
                                              @RequestParam("amount") Double amount) {
        return cryptoWalletService.addCryptoToWallet(email, currencyCode, amount);
    }
}
