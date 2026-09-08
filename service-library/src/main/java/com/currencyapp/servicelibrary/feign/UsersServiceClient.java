package com.currencyapp.servicelibrary.feign;

import com.currencyapp.servicelibrary.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "users-service")
public interface UsersServiceClient {

    @GetMapping("/users/email")
    UserDto getUserByEmail(@RequestParam("email") String email);
}
