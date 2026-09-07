package com.currencyapp.servicelibrary.feign;

import com.currencyapp.servicelibrary.dto.CryptoWalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "crypto-wallet", configuration = InternalAuthFeignConfig.class)
public interface CryptoWalletServiceClient {

    @PostMapping("/crypto-wallets/internal")
    void createWallet(@RequestParam("email") String email);

    @DeleteMapping("/crypto-wallets/internal")
    void deleteWalletByEmail(@RequestParam("email") String email);

    @GetMapping("/crypto-wallets/balance")
    CryptoWalletDto getBalance(@RequestParam("email") String email, @RequestParam("currency") String currency);

    @PostMapping("/crypto-wallets/deduct")
    CryptoWalletDto deductAmount(@RequestParam("email") String email,
                                  @RequestParam("currency") String currency,
                                  @RequestParam("amount") Double amount);

    @PostMapping("/crypto-wallets/add")
    CryptoWalletDto addCryptoToWallet(@RequestParam("email") String email,
                                       @RequestParam("currency") String currency,
                                       @RequestParam("amount") Double amount);
}
