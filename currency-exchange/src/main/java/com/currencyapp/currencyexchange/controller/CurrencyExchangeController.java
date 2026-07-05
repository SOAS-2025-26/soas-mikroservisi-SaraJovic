package com.currencyapp.currencyexchange.controller;

import com.currencyapp.currencyexchange.service.CurrencyExchangeService;
import com.currencyapp.servicelibrary.dto.ExchangeRateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency-exchange")
@RequiredArgsConstructor
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    @GetMapping
    public ExchangeRateDto getExchangeRate(@RequestParam("from") String from, @RequestParam("to") String to) {
        return currencyExchangeService.getExchangeRate(from, to);
    }
}
