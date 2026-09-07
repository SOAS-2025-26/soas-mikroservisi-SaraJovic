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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
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
        from = from.toUpperCase();
        to = to.toUpperCase();

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
        if (e instanceof BusinessException businessException) {
            log.warn("Trade rejected for from={}, to={}, quantity={}, email={}: {}",
                    from, to, quantity, email, businessException.getMessage());
            throw businessException;
        }
        log.error("Trade fallback triggered for from={}, to={}, quantity={}, email={}: {} - {}",
                from, to, quantity, email, e.getClass().getName(), e.getMessage(), e);
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

        CryptoWalletDto updatedWallet;
        try {
            updatedWallet = cryptoWalletServiceClient.addCryptoToWallet(email, to, convertedAmount);
        } catch (Exception ex) {
            log.warn("Failed to credit {} {} to {} after deduction; refunding {} {}", convertedAmount, to, email, quantity, from);
            cryptoWalletServiceClient.addCryptoToWallet(email, from, quantity);
            throw new BusinessException("Trade failed while crediting target wallet; deducted amount has been refunded.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

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
            effectiveQuantity = conversionResult.getConvertedAmount();
            fiatCurrency = "USD";
        }

        ExchangeRateDto rate = cryptoExchangeServiceClient.getExchangeRate(to, fiatCurrency);
        BankAccountDto bankAccount = bankAccountServiceClient.getBalance(email, fiatCurrency);

        if (bankAccount.getAmount() < effectiveQuantity) {
            throw new BusinessException("Insufficient fiat funds", HttpStatus.BAD_REQUEST);
        }

        Double convertedAmount = effectiveQuantity / rate.getRate();

        bankAccountServiceClient.deductAmount(email, fiatCurrency, effectiveQuantity);

        CryptoWalletDto updatedWallet;
        try {
            updatedWallet = cryptoWalletServiceClient.addCryptoToWallet(email, to, convertedAmount);
        } catch (Exception ex) {
            log.warn("Failed to credit {} {} to {} after deduction; refunding {} {}", convertedAmount, to, email, effectiveQuantity, fiatCurrency);
            bankAccountServiceClient.addCurrencyToAccount(email, fiatCurrency, effectiveQuantity);
            throw new BusinessException("Trade failed while crediting target wallet; deducted amount has been refunded.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

        BankAccountDto updatedBankAccount;
        try {
            updatedBankAccount = bankAccountServiceClient.addCurrencyToAccount(email, to, finalAmount);
        } catch (Exception ex) {
            log.warn("Failed to credit {} {} to {} after deduction; refunding {} {}", finalAmount, to, email, quantity, from);
            cryptoWalletServiceClient.addCryptoToWallet(email, from, quantity);
            throw new BusinessException("Trade failed while crediting target account; deducted amount has been refunded.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return TradeResultDto.builder()
                .bankAccount(updatedBankAccount)
                .cryptoWallet(null)
                .transactionMessage("Successfully exchanged " + from + ": " + quantity + " for " + to + ": " + finalAmount)
                .build();
    }
}
