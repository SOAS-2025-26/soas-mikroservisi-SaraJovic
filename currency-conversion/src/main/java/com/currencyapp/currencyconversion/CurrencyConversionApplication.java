package com.currencyapp.currencyconversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.currencyapp.servicelibrary.feign"})
public class CurrencyConversionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyConversionApplication.class, args);
    }
}
