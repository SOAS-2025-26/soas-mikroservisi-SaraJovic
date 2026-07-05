package com.currencyapp.cryptoexchange.service;

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
public class CryptoExchangeService {

    private static final String COINGECKO_URL =
            "https://api.coingecko.com/api/v3/simple/price?ids={coinId}&vs_currencies={to}";

    private static final Map<String, String> SYMBOL_TO_COIN_ID = Map.ofEntries(
            Map.entry("BTC", "bitcoin"),
            Map.entry("ETH", "ethereum"),
            Map.entry("BNB", "binancecoin"),
            Map.entry("SOL", "solana"),
            Map.entry("ADA", "cardano"),
            Map.entry("DOGE", "dogecoin"),
            Map.entry("XRP", "ripple"),
            Map.entry("DOT", "polkadot"),
            Map.entry("AVAX", "avalanche-2"),
            Map.entry("MATIC", "matic-network")
    );

    private final RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public ExchangeRateDto getExchangeRate(String from, String to) {
        String coinId = SYMBOL_TO_COIN_ID.get(from.toUpperCase());

        if (coinId == null) {
            throw new BusinessException("Unsupported cryptocurrency: " + from, HttpStatus.BAD_REQUEST);
        }

        String toLower = to.toLowerCase();
        Map<String, Map<String, Object>> response;

        try {
            response = restTemplate.getForObject(COINGECKO_URL, Map.class, coinId, toLower);
        } catch (RestClientException ex) {
            throw new BusinessException("Failed to retrieve exchange rate from " + from + " to " + to, HttpStatus.BAD_REQUEST);
        }

        if (response == null || !response.containsKey(coinId)) {
            throw new BusinessException("No exchange rate data returned for " + from + " to " + to, HttpStatus.BAD_REQUEST);
        }

        Object rateValue = response.get(coinId).get(toLower);

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
