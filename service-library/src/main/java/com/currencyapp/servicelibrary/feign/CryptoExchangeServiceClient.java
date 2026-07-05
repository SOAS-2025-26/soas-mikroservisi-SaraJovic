package com.currencyapp.servicelibrary.feign;

import com.currencyapp.servicelibrary.dto.ExchangeRateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "crypto-exchange")
public interface CryptoExchangeServiceClient {

    @GetMapping("/crypto-exchange")
    ExchangeRateDto getExchangeRate(@RequestParam("from") String from, @RequestParam("to") String to);
}
