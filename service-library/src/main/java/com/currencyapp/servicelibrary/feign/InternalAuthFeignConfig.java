package com.currencyapp.servicelibrary.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalAuthFeignConfig {

    private static final String INTERNAL_ROLE_HEADER = "X-User-Role";
    private static final String INTERNAL_ROLE_VALUE = "ADMIN";

    @Bean
    public RequestInterceptor internalAuthRequestInterceptor() {
        return requestTemplate -> requestTemplate.header(INTERNAL_ROLE_HEADER, INTERNAL_ROLE_VALUE);
    }
}
