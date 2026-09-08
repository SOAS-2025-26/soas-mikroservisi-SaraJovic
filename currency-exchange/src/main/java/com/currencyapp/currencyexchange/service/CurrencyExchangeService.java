package com.currencyapp.currencyexchange.service;

import com.currencyapp.servicelibrary.dto.ExchangeRateDto;
import com.currencyapp.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeService {

    private static final String OPEN_ER_API_URL = "https://open.er-api.com/v6/latest/{from}";

    private final RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public ExchangeRateDto getExchangeRate(String from, String to) {
        Map<String, Object> response;

        try {
            response = restTemplate.getForObject(OPEN_ER_API_URL, Map.class, from);
        } catch (RestClientException ex) {
            throw new BusinessException("Failed to retrieve exchange rate from " + from + " to " + to, HttpStatus.BAD_REQUEST);
        }

        if (response == null || !"success".equals(response.get("result"))) {
            throw new BusinessException("Currency exchange rate service returned an error", HttpStatus.BAD_GATEWAY);
        }

        if (!response.containsKey("rates")) {
            throw new BusinessException("No exchange rate data returned for " + from + " to " + to, HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        Object rateValue = rates.get(to.toUpperCase());

        if (rateValue == null) {
            throw new BusinessException("Currency " + to + " not found in exchange rate response", HttpStatus.BAD_REQUEST);
        }

        Double rate = ((Number) rateValue).doubleValue();

        return ExchangeRateDto.builder()
                .from(from)
                .to(to)
                .rate(rate)
                .build();
    }
}
