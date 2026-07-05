package com.currencyapp.tradeservice.controller;

import com.currencyapp.servicelibrary.dto.TradeResultDto;
import com.currencyapp.tradeservice.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade-service")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping
    public TradeResultDto trade(@RequestParam("from") String from,
                                 @RequestParam("to") String to,
                                 @RequestParam("quantity") Double quantity,
                                 @RequestHeader("X-User-Email") String userEmail) {
        return tradeService.trade(from, to, quantity, userEmail);
    }
}
