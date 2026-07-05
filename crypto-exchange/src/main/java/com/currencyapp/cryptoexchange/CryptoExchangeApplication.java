package com.currencyapp.cryptoexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CryptoExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoExchangeApplication.class, args);
    }
}
