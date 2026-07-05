package com.currencyapp.servicelibrary.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "users-service")
public interface UserServiceClient {
}
