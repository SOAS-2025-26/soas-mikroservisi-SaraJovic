package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "currency-conversion")
public interface CurrencyConversionServiceClient {
}
