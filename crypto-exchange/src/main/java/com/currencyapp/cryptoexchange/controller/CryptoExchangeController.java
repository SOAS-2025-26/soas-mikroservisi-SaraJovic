package com.currencyapp.cryptoexchange.controller;

import com.currencyapp.cryptoexchange.service.CryptoExchangeService;
import com.currencyapp.servicelibrary.dto.ExchangeRateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crypto-exchange")
@RequiredArgsConstructor
public class CryptoExchangeController {

    private final CryptoExchangeService cryptoExchangeService;

    @GetMapping
    public ExchangeRateDto getExchangeRate(@RequestParam("from") String from, @RequestParam("to") String to) {
        return cryptoExchangeService.getExchangeRate(from, to);
    }
}
