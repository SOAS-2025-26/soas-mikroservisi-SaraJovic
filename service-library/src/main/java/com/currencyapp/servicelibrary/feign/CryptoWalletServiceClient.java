package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletServiceClient {
}
