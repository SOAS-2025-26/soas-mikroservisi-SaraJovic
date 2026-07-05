package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "currency-exchange")
public interface CurrencyExchangeServiceClient {
}
