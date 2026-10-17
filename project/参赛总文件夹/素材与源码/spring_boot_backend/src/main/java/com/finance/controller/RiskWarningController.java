package com.finance.controller;

import com.finance.service.RiskWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskWarningController {

    @Autowired
    private RiskWarningService riskWarningService;

    @GetMapping("/profile/{userId}")
    public Map<String, Object> getUserRiskProfile(@PathVariable Integer userId) {
        return riskWarningService.getUserRiskProfile(userId);
    }

    @PostMapping("/profile")
    public Map<String, Object> setUserRiskProfile(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        String riskType = (String) params.get("riskType");
        Integer riskScore = params.get("riskScore") != null ? (Integer) params.get("riskScore") : null;
        return riskWarningService.setUserRiskProfile(userId, riskType, riskScore);
    }

    @GetMapping("/check/{userId}/{stockCode}")
    public Map<String, Object> checkStockRiskMatch(
            @PathVariable Integer userId,
            @PathVariable String stockCode) {
        return riskWarningService.checkStockRiskMatch(userId, stockCode);
    }

    @GetMapping("/portfolio/{userId}")
    public Map<String, Object> checkPortfolioRisk(@PathVariable Integer userId) {
        return riskWarningService.checkPortfolioRisk(userId);
    }

    @GetMapping("/types")
    public List<Map<String, Object>> getRiskTypes() {
        return riskWarningService.getRiskTypes();
    }
}
