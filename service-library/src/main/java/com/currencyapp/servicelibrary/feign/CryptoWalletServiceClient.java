package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletServiceClient {

    @PostMapping("/crypto-wallets/internal")
    void createWallet(@RequestParam("email") String email);

    @DeleteMapping("/crypto-wallets/internal")
    void deleteWalletByEmail(@RequestParam("email") String email);
}
