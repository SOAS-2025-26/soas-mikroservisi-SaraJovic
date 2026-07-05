package com.currencyapp.servicelibrary.feign;

import com.currencyapp.servicelibrary.dto.ConversionResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "currency-conversion")
public interface CurrencyConversionServiceClient {

    @GetMapping("/currency-conversion")
    ConversionResultDto convert(@RequestParam("from") String from,
                                 @RequestParam("to") String to,
                                 @RequestParam("quantity") Double quantity,
                                 @RequestHeader("X-User-Email") String email);
}
