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

    private static final String FRANKFURTER_URL = "https://api.frankfurter.app/latest?from={from}&to={to}";

    private final RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public ExchangeRateDto getExchangeRate(String from, String to) {
        Map<String, Object> response;

        try {
            response = restTemplate.getForObject(FRANKFURTER_URL, Map.class, from, to);
        } catch (RestClientException ex) {
            throw new BusinessException("Failed to retrieve exchange rate from " + from + " to " + to, HttpStatus.BAD_REQUEST);
        }

        if (response == null || !response.containsKey("rates")) {
            throw new BusinessException("No exchange rate data returned for " + from + " to " + to, HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        Object rateValue = rates.get(to);

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
