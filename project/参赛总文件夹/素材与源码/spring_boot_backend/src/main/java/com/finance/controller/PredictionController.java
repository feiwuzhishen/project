package com.finance.controller;

import com.finance.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/create")
    public Map<String, Object> createPrediction(
            @RequestParam Integer userId,
            @RequestParam String stockCode,
            @RequestParam String predictionType) {
        return predictionService.createPrediction(userId, stockCode, predictionType);
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserPredictions(@PathVariable Integer userId) {
        return predictionService.getUserPredictions(userId);
    }

    @GetMapping("/accuracy/{userId}")
    public Map<String, Object> getPredictionAccuracy(@PathVariable Integer userId) {
        return predictionService.getPredictionAccuracy(userId);
    }

    @PostMapping("/check")
    public Map<String, Object> checkPredictionResult(@RequestParam Integer predictionId) {
        return predictionService.checkPredictionResult(predictionId);
    }

    @GetMapping("/analyze/{stockCode}")
    public Map<String, Object> analyzeTrend(@PathVariable String stockCode) {
        return predictionService.analyzeTrend(stockCode);
    }
}
