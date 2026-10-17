package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.finance.entity.InvestmentPrediction;
import com.finance.entity.StockRealtimeData;
import com.finance.mapper.InvestmentPredictionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PredictionService {

    @Autowired
    private InvestmentPredictionMapper predictionMapper;

    @Autowired
    private StockDataService stockDataService;

    private static final BigDecimal ACCURACY_THRESHOLD = new BigDecimal("0.05");

    public Map<String, Object> createPrediction(Integer userId, String stockCode, String predictionType) {
        Map<String, Object> result = new HashMap<>();
        try {
            StockRealtimeData stockData = stockDataService.getRealtimeData(stockCode);
            if (stockData == null || stockData.getCurrentPrice() == null) {
                result.put("success", false);
                result.put("error", "无法获取股票数据");
                return result;
            }

            BigDecimal currentPrice = BigDecimal.valueOf(stockData.getCurrentPrice());
            BigDecimal predictedPrice = calculatePredictedPrice(currentPrice, predictionType);
            BigDecimal confidence = calculateConfidence(stockData, predictionType);

            InvestmentPrediction prediction = new InvestmentPrediction();
            prediction.setUserId(userId);
            prediction.setStockCode(stockCode);
            prediction.setStockName(stockData.getName());
            prediction.setPredictionType(predictionType);
            prediction.setPredictedPrice(predictedPrice);
            prediction.setConfidence(confidence);
            prediction.setPredictionDate(LocalDateTime.now());
            prediction.setCreatedAt(LocalDateTime.now());

            if ("short".equals(predictionType)) {
                prediction.setResultDate(LocalDateTime.now().plusDays(7));
            } else {
                prediction.setResultDate(LocalDateTime.now().plusDays(30));
            }

            predictionMapper.insert(prediction);

            Map<String, Object> data = new HashMap<>();
            data.put("id", prediction.getId());
            data.put("stockCode", stockCode);
            data.put("stockName", stockData.getName());
            data.put("currentPrice", currentPrice);
            data.put("predictedPrice", predictedPrice);
            data.put("confidence", confidence);
            data.put("predictionType", predictionType);
            data.put("predictionDate", prediction.getPredictionDate());
            data.put("resultDate", prediction.getResultDate());

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private BigDecimal calculatePredictedPrice(BigDecimal currentPrice, String predictionType) {
        Random random = new Random();
        double trendFactor;
        double volatility;

        if ("short".equals(predictionType)) {
            trendFactor = (random.nextDouble() - 0.5) * 0.1;
            volatility = 0.05;
        } else {
            trendFactor = (random.nextDouble() - 0.5) * 0.2;
            volatility = 0.15;
        }

        double change = trendFactor + (random.nextDouble() - 0.5) * volatility;
        BigDecimal predictedPrice = currentPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(change)));
        return predictedPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateConfidence(StockRealtimeData stockData, String predictionType) {
        double baseConfidence = 0.5;

        if (stockData.getChangePercent() != null) {
            double absChange = Math.abs(stockData.getChangePercent());
            if (absChange < 2) {
                baseConfidence += 0.15;
            } else if (absChange < 5) {
                baseConfidence += 0.05;
            } else {
                baseConfidence -= 0.1;
            }
        }

        if ("short".equals(predictionType)) {
            baseConfidence += 0.1;
        }

        baseConfidence = Math.max(0.3, Math.min(0.85, baseConfidence));

        return BigDecimal.valueOf(baseConfidence).setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> getUserPredictions(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<InvestmentPrediction> predictions = predictionMapper.selectList(
                new LambdaQueryWrapper<InvestmentPrediction>()
                    .eq(InvestmentPrediction::getUserId, userId)
                    .orderByDesc(InvestmentPrediction::getCreatedAt)
            );

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (InvestmentPrediction pred : predictions) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", pred.getId());
                item.put("stockCode", pred.getStockCode());
                item.put("stockName", pred.getStockName());
                item.put("predictionType", pred.getPredictionType());
                item.put("predictedPrice", pred.getPredictedPrice());
                item.put("actualPrice", pred.getActualPrice());
                item.put("confidence", pred.getConfidence());
                item.put("predictionDate", pred.getPredictionDate());
                item.put("resultDate", pred.getResultDate());
                item.put("isAccurate", pred.getIsAccurate());
                item.put("createdAt", pred.getCreatedAt());
                dataList.add(item);
            }

            result.put("success", true);
            result.put("data", dataList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getPredictionAccuracy(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> accuracyData = predictionMapper.countAccuracyByUserId(userId);
            
            int total = 0;
            int accurate = 0;
            
            if (accuracyData != null) {
                Object totalObj = accuracyData.get("total");
                Object accurateObj = accuracyData.get("accurate");
                
                if (totalObj != null) {
                    total = ((Number) totalObj).intValue();
                }
                if (accurateObj != null) {
                    accurate = ((Number) accurateObj).intValue();
                }
            }

            double accuracyRate = total > 0 ? (double) accurate / total * 100 : 0;

            List<Map<String, Object>> typeAccuracy = predictionMapper.countAccuracyByType(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("totalPredictions", total);
            data.put("accuratePredictions", accurate);
            data.put("accuracyRate", Math.round(accuracyRate * 100) / 100.0);
            data.put("typeAccuracy", typeAccuracy);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> checkPredictionResult(Integer predictionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            InvestmentPrediction prediction = predictionMapper.selectById(predictionId);
            if (prediction == null) {
                result.put("success", false);
                result.put("error", "预测记录不存在");
                return result;
            }

            StockRealtimeData stockData = stockDataService.getRealtimeData(prediction.getStockCode());
            if (stockData == null || stockData.getCurrentPrice() == null) {
                result.put("success", false);
                result.put("error", "无法获取股票数据");
                return result;
            }

            BigDecimal actualPrice = BigDecimal.valueOf(stockData.getCurrentPrice());
            BigDecimal predictedPrice = prediction.getPredictedPrice();
            
            BigDecimal diff = actualPrice.subtract(predictedPrice).abs();
            BigDecimal threshold = predictedPrice.multiply(ACCURACY_THRESHOLD);
            boolean isAccurate = diff.compareTo(threshold) <= 0;

            prediction.setActualPrice(actualPrice);
            prediction.setIsAccurate(isAccurate);
            prediction.setResultDate(LocalDateTime.now());
            predictionMapper.updateById(prediction);

            Map<String, Object> data = new HashMap<>();
            data.put("id", prediction.getId());
            data.put("stockCode", prediction.getStockCode());
            data.put("stockName", prediction.getStockName());
            data.put("predictedPrice", predictedPrice);
            data.put("actualPrice", actualPrice);
            data.put("isAccurate", isAccurate);
            data.put("difference", actualPrice.subtract(predictedPrice));
            data.put("differencePercent", actualPrice.subtract(predictedPrice)
                .divide(predictedPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> analyzeTrend(String stockCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            StockRealtimeData stockData = stockDataService.getRealtimeData(stockCode);
            if (stockData == null || stockData.getCurrentPrice() == null) {
                result.put("success", false);
                result.put("error", "无法获取股票数据");
                return result;
            }

            BigDecimal currentPrice = BigDecimal.valueOf(stockData.getCurrentPrice());
            BigDecimal preClosePrice = BigDecimal.valueOf(stockData.getPreClosePrice());
            
            double dailyChange = stockData.getChangePercent() != null ? stockData.getChangePercent() : 0;
            
            String shortTermTrend;
            String midTermTrend;
            double shortTermConfidence;
            double midTermConfidence;

            if (dailyChange > 2) {
                shortTermTrend = "上涨";
                shortTermConfidence = 0.6 + Math.min(dailyChange * 0.02, 0.2);
            } else if (dailyChange < -2) {
                shortTermTrend = "下跌";
                shortTermConfidence = 0.6 + Math.min(Math.abs(dailyChange) * 0.02, 0.2);
            } else {
                shortTermTrend = "震荡";
                shortTermConfidence = 0.55;
            }

            double midTermFactor = dailyChange * 0.5 + (Math.random() - 0.5) * 2;
            if (midTermFactor > 1) {
                midTermTrend = "上涨";
                midTermConfidence = 0.5 + Math.min(midTermFactor * 0.05, 0.25);
            } else if (midTermFactor < -1) {
                midTermTrend = "下跌";
                midTermConfidence = 0.5 + Math.min(Math.abs(midTermFactor) * 0.05, 0.25);
            } else {
                midTermTrend = "震荡";
                midTermConfidence = 0.45;
            }

            BigDecimal shortTermTarget = currentPrice.multiply(
                BigDecimal.ONE.add(BigDecimal.valueOf(dailyChange > 0 ? 0.03 : -0.03))
            ).setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal midTermTarget = currentPrice.multiply(
                BigDecimal.ONE.add(BigDecimal.valueOf(midTermFactor > 0 ? 0.08 : -0.08))
            ).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> data = new HashMap<>();
            data.put("stockCode", stockCode);
            data.put("stockName", stockData.getName());
            data.put("currentPrice", currentPrice);
            
            Map<String, Object> shortTerm = new HashMap<>();
            shortTerm.put("trend", shortTermTrend);
            shortTerm.put("confidence", Math.round(shortTermConfidence * 100) / 100.0);
            shortTerm.put("targetPrice", shortTermTarget);
            shortTerm.put("period", "1周");
            data.put("shortTerm", shortTerm);
            
            Map<String, Object> midTerm = new HashMap<>();
            midTerm.put("trend", midTermTrend);
            midTerm.put("confidence", Math.round(midTermConfidence * 100) / 100.0);
            midTerm.put("targetPrice", midTermTarget);
            midTerm.put("period", "1月");
            data.put("midTerm", midTerm);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
