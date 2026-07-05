package com.currencyapp.currencyconversion.controller;

import com.currencyapp.currencyconversion.service.CurrencyConversionService;
import com.currencyapp.servicelibrary.dto.ConversionResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency-conversion")
@RequiredArgsConstructor
public class CurrencyConversionController {

    private final CurrencyConversionService currencyConversionService;

    @GetMapping
    public ConversionResultDto convert(@RequestParam("from") String from,
                                        @RequestParam("to") String to,
                                        @RequestParam("quantity") Double quantity,
                                        @RequestHeader("X-User-Email") String userEmail) {
        return currencyConversionService.convert(from, to, quantity, userEmail);
    }
}
