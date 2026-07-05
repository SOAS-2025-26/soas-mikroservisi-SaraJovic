package com.currencyapp.cryptowallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CryptoWalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoWalletApplication.class, args);
    }
}
