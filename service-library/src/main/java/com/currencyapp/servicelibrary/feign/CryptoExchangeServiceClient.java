package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "crypto-exchange")
public interface CryptoExchangeServiceClient {
}
