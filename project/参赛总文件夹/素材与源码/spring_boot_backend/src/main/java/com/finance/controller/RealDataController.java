package com.finance.controller;

import com.finance.service.RealDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/real")
@CrossOrigin(origins = "*")
public class RealDataController {

    @Autowired
    private RealDataService realDataService;

    @GetMapping("/crypto")
    public Map<String, Object> getCryptoData() {
        return realDataService.getCryptoData();
    }

    @GetMapping("/exchange-rates")
    public Map<String, Object> getExchangeRates() {
        return realDataService.getExchangeRates();
    }

    @GetMapping("/global-market")
    public Map<String, Object> getGlobalMarketData() {
        return realDataService.getGlobalMarketData();
    }

    @GetMapping("/stock/{symbol}")
    public Map<String, Object> getStockData(@PathVariable String symbol) {
        return realDataService.getStockData(symbol);
    }

    @GetMapping("/top-coins")
    public Map<String, Object> getTopCoins() {
        return realDataService.getTopCoins();
    }

    @GetMapping("/forex")
    public Map<String, Object> getForexData() {
        return realDataService.getForexData();
    }

    @GetMapping("/test")
    public Map<String, Object> testApi() {
        return realDataService.testApi();
    }
}
