package com.finance.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.service.InvestmentAnalysisService;
import com.finance.vo.Result;

@RestController
@RequestMapping("/api/analysis")
public class InvestmentAnalysisController {

    private final InvestmentAnalysisService investmentAnalysisService;

    public InvestmentAnalysisController(InvestmentAnalysisService investmentAnalysisService) {
        this.investmentAnalysisService = investmentAnalysisService;
    }

    @GetMapping("/fundamental/{stockCode}")
    public Result<Map<String, Object>> getFundamentalAnalysis(@PathVariable String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return Result.error(400, "股票代码不能为空");
        }

        Map<String, Object> analysis = investmentAnalysisService.analyzeFundamental(stockCode);
        
        if (!(Boolean) analysis.getOrDefault("success", false)) {
            return Result.error(404, (String) analysis.getOrDefault("message", "分析失败"));
        }

        return Result.success(analysis);
    }

    @GetMapping("/technical/{stockCode}")
    public Result<Map<String, Object>> getTechnicalAnalysis(@PathVariable String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return Result.error(400, "股票代码不能为空");
        }

        Map<String, Object> analysis = investmentAnalysisService.analyzeTechnical(stockCode);
        
        if (!(Boolean) analysis.getOrDefault("success", false)) {
            return Result.error(404, (String) analysis.getOrDefault("message", "分析失败"));
        }

        return Result.success(analysis);
    }

    @GetMapping("/suggestion/{stockCode}")
    public Result<Map<String, Object>> getInvestmentSuggestion(@PathVariable String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return Result.error(400, "股票代码不能为空");
        }

        Map<String, Object> suggestion = investmentAnalysisService.generateSuggestion(stockCode);
        
        if (!(Boolean) suggestion.getOrDefault("success", false)) {
            return Result.error(404, (String) suggestion.getOrDefault("message", "获取建议失败"));
        }

        return Result.success(suggestion);
    }

    @PostMapping("/trade-reason")
    public Result<Map<String, Object>> getTradeReasonAnalysis(@RequestBody Map<String, Object> request) {
        String stockCode = (String) request.get("stockCode");
        String tradeType = (String) request.get("tradeType");
        Integer quantity = (Integer) request.get("quantity");
        Double price = ((Number) request.get("price")).doubleValue();

        if (stockCode == null || stockCode.trim().isEmpty()) {
            return Result.error(400, "股票代码不能为空");
        }

        if (tradeType == null || (!tradeType.equals("buy") && !tradeType.equals("sell"))) {
            return Result.error(400, "交易类型无效");
        }

        if (quantity == null || quantity <= 0) {
            return Result.error(400, "交易数量无效");
        }

        if (price == null || price <= 0) {
            return Result.error(400, "交易价格无效");
        }

        Map<String, Object> analysis = investmentAnalysisService.analyzeTradeReason(
            stockCode, tradeType, quantity, price
        );

        if (!(Boolean) analysis.getOrDefault("success", false)) {
            return Result.error(404, (String) analysis.getOrDefault("message", "分析失败"));
        }

        return Result.success(analysis);
    }
}
