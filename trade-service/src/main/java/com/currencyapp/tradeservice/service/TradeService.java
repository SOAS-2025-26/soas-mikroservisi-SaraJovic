package com.currencyapp.tradeservice.service;

import com.currencyapp.servicelibrary.dto.BankAccountDto;
import com.currencyapp.servicelibrary.dto.ConversionResultDto;
import com.currencyapp.servicelibrary.dto.CryptoWalletDto;
import com.currencyapp.servicelibrary.dto.ExchangeRateDto;
import com.currencyapp.servicelibrary.dto.TradeResultDto;
import com.currencyapp.servicelibrary.feign.BankAccountServiceClient;
import com.currencyapp.servicelibrary.feign.CryptoExchangeServiceClient;
import com.currencyapp.servicelibrary.feign.CryptoWalletServiceClient;
import com.currencyapp.servicelibrary.feign.CurrencyConversionServiceClient;
import com.currencyapp.servicelibrary.feign.CurrencyExchangeServiceClient;
import com.currencyapp.util.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TradeService {

    private static final Set<String> CRYPTO_CURRENCIES = Set.of(
            "BTC", "ETH", "BNB", "SOL", "ADA", "DOGE", "XRP", "DOT", "AVAX", "MATIC");

    private static final Set<String> FIAT_CURRENCIES = Set.of(
            "USD", "EUR", "RSD", "GBP", "CHF", "JPY", "CAD", "AUD");

    private final CryptoExchangeServiceClient cryptoExchangeServiceClient;
    private final CurrencyExchangeServiceClient currencyExchangeServiceClient;
    private final CurrencyConversionServiceClient currencyConversionServiceClient;
    private final BankAccountServiceClient bankAccountServiceClient;
    private final CryptoWalletServiceClient cryptoWalletServiceClient;

    private boolean isCrypto(String code) {
        return CRYPTO_CURRENCIES.contains(code.toUpperCase());
    }

    private boolean isFiat(String code) {
        return FIAT_CURRENCIES.contains(code.toUpperCase());
    }

    @CircuitBreaker(name = "tradeService", fallbackMethod = "tradeFallback")
    public TradeResultDto trade(String from, String to, Double quantity, String email) {
        if (isCrypto(from) && isCrypto(to)) {
            return tradeCryptoToCrypto(from, to, quantity, email);
        }

        if (isFiat(from) && isCrypto(to)) {
            return tradeFiatToCrypto(from, to, quantity, email);
        }

        if (isCrypto(from) && isFiat(to)) {
            return tradeCryptoToFiat(from, to, quantity, email);
        }

        throw new BusinessException("Invalid currency pair", HttpStatus.BAD_REQUEST);
    }

    public TradeResultDto tradeFallback(String from, String to, Double quantity, String email, Exception e) {
        throw new BusinessException("Trade service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    private TradeResultDto tradeCryptoToCrypto(String from, String to, Double quantity, String email) {
        ExchangeRateDto rate = cryptoExchangeServiceClient.getExchangeRate(from, to);
        CryptoWalletDto wallet = cryptoWalletServiceClient.getBalance(email, from);

        if (wallet.getAmount() < quantity) {
            throw new BusinessException("Insufficient crypto funds", HttpStatus.BAD_REQUEST);
        }

        Double convertedAmount = quantity * rate.getRate();

        cryptoWalletServiceClient.deductAmount(email, from, quantity);
        CryptoWalletDto updatedWallet = cryptoWalletServiceClient.addCryptoToWallet(email, to, convertedAmount);

        return TradeResultDto.builder()
                .bankAccount(null)
                .cryptoWallet(updatedWallet)
                .transactionMessage("Successfully exchanged " + from + ": " + quantity + " for " + to + ": " + convertedAmount)
                .build();
    }

    private TradeResultDto tradeFiatToCrypto(String from, String to, Double quantity, String email) {
        String fiatCurrency = from;
        Double effectiveQuantity = quantity;

        if (!"USD".equalsIgnoreCase(from) && !"EUR".equalsIgnoreCase(from)) {
            ConversionResultDto conversionResult = currencyConversionServiceClient.convert(from, "USD", quantity, email);
            effectiveQuantity = extractConvertedAmount(conversionResult.getTransactionMessage());
            fiatCurrency = "USD";
        }

        ExchangeRateDto rate = cryptoExchangeServiceClient.getExchangeRate(fiatCurrency, to);
        BankAccountDto bankAccount = bankAccountServiceClient.getBalance(email, fiatCurrency);

        if (bankAccount.getAmount() < effectiveQuantity) {
            throw new BusinessException("Insufficient fiat funds", HttpStatus.BAD_REQUEST);
        }

        Double convertedAmount = effectiveQuantity * rate.getRate();

        bankAccountServiceClient.deductAmount(email, fiatCurrency, effectiveQuantity);
        CryptoWalletDto updatedWallet = cryptoWalletServiceClient.addCryptoToWallet(email, to, convertedAmount);

        return TradeResultDto.builder()
                .bankAccount(null)
                .cryptoWallet(updatedWallet)
                .transactionMessage("Successfully exchanged " + fiatCurrency + ": " + effectiveQuantity + " for " + to + ": " + convertedAmount)
                .build();
    }

    private TradeResultDto tradeCryptoToFiat(String from, String to, Double quantity, String email) {
        CryptoWalletDto wallet = cryptoWalletServiceClient.getBalance(email, from);

        if (wallet.getAmount() < quantity) {
            throw new BusinessException("Insufficient crypto funds", HttpStatus.BAD_REQUEST);
        }

        Double finalAmount;

        if (!"USD".equalsIgnoreCase(to) && !"EUR".equalsIgnoreCase(to)) {
            ExchangeRateDto cryptoToUsdRate = cryptoExchangeServiceClient.getExchangeRate(from, "USD");
            ExchangeRateDto usdToFiatRate = currencyExchangeServiceClient.getExchangeRate("USD", to);
            finalAmount = quantity * cryptoToUsdRate.getRate() * usdToFiatRate.getRate();
        } else {
            ExchangeRateDto rate = cryptoExchangeServiceClient.getExchangeRate(from, to);
            finalAmount = quantity * rate.getRate();
        }

        cryptoWalletServiceClient.deductAmount(email, from, quantity);
        BankAccountDto updatedBankAccount = bankAccountServiceClient.addCurrencyToAccount(email, to, finalAmount);

        return TradeResultDto.builder()
                .bankAccount(updatedBankAccount)
                .cryptoWallet(null)
                .transactionMessage("Successfully exchanged " + from + ": " + quantity + " for " + to + ": " + finalAmount)
                .build();
    }

    private Double extractConvertedAmount(String transactionMessage) {
        String[] parts = transactionMessage.split(": ");
        return Double.parseDouble(parts[parts.length - 1]);
    }
}
