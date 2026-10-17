package com.finance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.finance.entity.StockRealtimeData;

@Service
public class InvestmentAnalysisService {

    private final StockDataService stockDataService;
    private final Random random = new Random();

    public InvestmentAnalysisService(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    public Map<String, Object> analyzeFundamental(String stockCode) {
        Map<String, Object> result = new HashMap<>();
        
        StockRealtimeData stockData = stockDataService.getRealtimeData(stockCode);
        if (stockData == null) {
            result.put("success", false);
            result.put("message", "无法获取股票数据");
            return result;
        }

        result.put("success", true);
        result.put("stockCode", stockCode);
        result.put("stockName", stockData.getName());
        result.put("currentPrice", stockData.getCurrentPrice());

        Map<String, Object> peAnalysis = analyzePE(stockData);
        result.put("peAnalysis", peAnalysis);

        Map<String, Object> pbAnalysis = analyzePB(stockData);
        result.put("pbAnalysis", pbAnalysis);

        Map<String, Object> marketCapAnalysis = analyzeMarketCap(stockData);
        result.put("marketCapAnalysis", marketCapAnalysis);

        Map<String, Object> industryAnalysis = analyzeIndustryPosition(stockData);
        result.put("industryAnalysis", industryAnalysis);

        String overallRating = calculateOverallRating(peAnalysis, pbAnalysis, marketCapAnalysis, industryAnalysis);
        result.put("overallRating", overallRating);

        return result;
    }

    private Map<String, Object> analyzePE(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        double pe = 10 + random.nextDouble() * 30;
        pe = Math.round(pe * 100) / 100.0;
        
        analysis.put("pe", pe);
        analysis.put("industryAvgPE", 20.0);
        
        String evaluation;
        String suggestion;
        
        if (pe < 15) {
            evaluation = "低估";
            suggestion = "市盈率较低，可能存在价值低估机会，建议关注";
        } else if (pe < 25) {
            evaluation = "合理";
            suggestion = "市盈率处于合理区间，估值相对正常";
        } else if (pe < 40) {
            evaluation = "偏高";
            suggestion = "市盈率偏高，需关注公司成长性是否支撑当前估值";
        } else {
            evaluation = "高估";
            suggestion = "市盈率过高，存在估值风险，建议谨慎";
        }
        
        analysis.put("evaluation", evaluation);
        analysis.put("suggestion", suggestion);
        
        return analysis;
    }

    private Map<String, Object> analyzePB(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        double pb = 1 + random.nextDouble() * 4;
        pb = Math.round(pb * 100) / 100.0;
        
        analysis.put("pb", pb);
        analysis.put("industryAvgPB", 2.0);
        
        String evaluation;
        String suggestion;
        
        if (pb < 1) {
            evaluation = "破净";
            suggestion = "股价低于净资产，可能存在安全边际，但也需关注公司经营状况";
        } else if (pb < 2) {
            evaluation = "低估";
            suggestion = "市净率较低，资产估值相对便宜";
        } else if (pb < 4) {
            evaluation = "合理";
            suggestion = "市净率处于合理区间";
        } else {
            evaluation = "偏高";
            suggestion = "市净率较高，需关注资产质量和盈利能力";
        }
        
        analysis.put("evaluation", evaluation);
        analysis.put("suggestion", suggestion);
        
        return analysis;
    }

    private Map<String, Object> analyzeMarketCap(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        double marketCap = 50 + random.nextDouble() * 5000;
        marketCap = Math.round(marketCap * 100) / 100.0;
        
        analysis.put("marketCap", marketCap);
        analysis.put("marketCapUnit", "亿");
        
        String scale;
        String characteristic;
        
        if (marketCap < 100) {
            scale = "小盘股";
            characteristic = "波动性较大，成长潜力高，但风险也相对较高";
        } else if (marketCap < 500) {
            scale = "中盘股";
            characteristic = "成长性与稳定性兼顾，适合中长期投资";
        } else if (marketCap < 2000) {
            scale = "大盘股";
            characteristic = "经营稳定，分红稳定，适合稳健型投资者";
        } else {
            scale = "超大盘股";
            characteristic = "行业龙头，流动性好，适合价值投资";
        }
        
        analysis.put("scale", scale);
        analysis.put("characteristic", characteristic);
        
        return analysis;
    }

    private Map<String, Object> analyzeIndustryPosition(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        String industry = getIndustryByStockCode(stockData.getCode());
        analysis.put("industry", industry);
        
        int rank = 1 + random.nextInt(10);
        analysis.put("industryRank", rank);
        
        double marketShare = random.nextDouble() * 30;
        marketShare = Math.round(marketShare * 100) / 100.0;
        analysis.put("marketShare", marketShare);
        
        String position;
        String suggestion;
        
        if (rank <= 3) {
            position = "行业龙头";
            suggestion = "行业地位稳固，具有较强的竞争优势和定价权";
        } else if (rank <= 5) {
            position = "行业领先";
            suggestion = "在行业中处于领先地位，发展前景良好";
        } else {
            position = "行业中游";
            suggestion = "行业竞争激烈，需关注公司的差异化竞争优势";
        }
        
        analysis.put("position", position);
        analysis.put("suggestion", suggestion);
        
        return analysis;
    }

    private String getIndustryByStockCode(String code) {
        Map<String, String> industryMap = new HashMap<>();
        industryMap.put("SH600000", "银行");
        industryMap.put("SH600519", "白酒");
        industryMap.put("SZ000001", "银行");
        industryMap.put("SH601318", "保险");
        industryMap.put("SH600036", "银行");
        industryMap.put("SZ000333", "家电");
        industryMap.put("SZ000651", "家电");
        industryMap.put("SZ000858", "白酒");
        industryMap.put("SZ002594", "汽车");
        industryMap.put("SH600030", "证券");
        
        return industryMap.getOrDefault(code, "综合");
    }

    private String calculateOverallRating(Map<String, Object> peAnalysis, 
                                          Map<String, Object> pbAnalysis,
                                          Map<String, Object> marketCapAnalysis,
                                          Map<String, Object> industryAnalysis) {
        int score = 0;
        
        String peEval = (String) peAnalysis.get("evaluation");
        if ("低估".equals(peEval)) score += 3;
        else if ("合理".equals(peEval)) score += 2;
        else if ("偏高".equals(peEval)) score += 1;
        
        String pbEval = (String) pbAnalysis.get("evaluation");
        if ("破净".equals(pbEval) || "低估".equals(pbEval)) score += 3;
        else if ("合理".equals(pbEval)) score += 2;
        else if ("偏高".equals(pbEval)) score += 1;
        
        int rank = (Integer) industryAnalysis.get("industryRank");
        if (rank <= 3) score += 3;
        else if (rank <= 5) score += 2;
        else score += 1;
        
        if (score >= 8) return "强烈推荐";
        if (score >= 6) return "推荐";
        if (score >= 4) return "中性";
        return "谨慎";
    }

    public Map<String, Object> analyzeTechnical(String stockCode) {
        Map<String, Object> result = new HashMap<>();
        
        StockRealtimeData stockData = stockDataService.getRealtimeData(stockCode);
        if (stockData == null) {
            result.put("success", false);
            result.put("message", "无法获取股票数据");
            return result;
        }

        result.put("success", true);
        result.put("stockCode", stockCode);
        result.put("stockName", stockData.getName());
        result.put("currentPrice", stockData.getCurrentPrice());

        Map<String, Object> maAnalysis = analyzeMovingAverages(stockData);
        result.put("maAnalysis", maAnalysis);

        Map<String, Object> rsiAnalysis = analyzeRSI(stockData);
        result.put("rsiAnalysis", rsiAnalysis);

        Map<String, Object> volumeAnalysis = analyzeVolume(stockData);
        result.put("volumeAnalysis", volumeAnalysis);

        List<Map<String, Object>> priceHistory = generatePriceHistory(stockData);
        result.put("priceHistory", priceHistory);

        String trend = determineTrend(maAnalysis, rsiAnalysis, volumeAnalysis);
        result.put("trend", trend);

        return result;
    }

    private Map<String, Object> analyzeMovingAverages(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        double currentPrice = stockData.getCurrentPrice();
        
        double ma5 = currentPrice * (0.95 + random.nextDouble() * 0.1);
        double ma10 = currentPrice * (0.90 + random.nextDouble() * 0.15);
        double ma20 = currentPrice * (0.85 + random.nextDouble() * 0.2);
        
        ma5 = Math.round(ma5 * 100) / 100.0;
        ma10 = Math.round(ma10 * 100) / 100.0;
        ma20 = Math.round(ma20 * 100) / 100.0;
        
        analysis.put("ma5", ma5);
        analysis.put("ma10", ma10);
        analysis.put("ma20", ma20);
        analysis.put("currentPrice", currentPrice);
        
        String maTrend;
        String suggestion;
        
        boolean aboveMA5 = currentPrice > ma5;
        boolean aboveMA10 = currentPrice > ma10;
        boolean aboveMA20 = currentPrice > ma20;
        
        if (aboveMA5 && aboveMA10 && aboveMA20) {
            maTrend = "多头排列";
            suggestion = "短期均线呈多头排列，趋势向上，可考虑持有或逢低买入";
        } else if (!aboveMA5 && !aboveMA10 && !aboveMA20) {
            maTrend = "空头排列";
            suggestion = "短期均线呈空头排列，趋势向下，建议谨慎操作";
        } else if (aboveMA5 && aboveMA10) {
            maTrend = "短期偏强";
            suggestion = "短期走势偏强，但中期趋势尚不明朗，建议观望";
        } else {
            maTrend = "震荡整理";
            suggestion = "股价在均线间震荡，方向不明，建议等待明确信号";
        }
        
        analysis.put("maTrend", maTrend);
        analysis.put("suggestion", suggestion);
        analysis.put("aboveMA5", aboveMA5);
        analysis.put("aboveMA10", aboveMA10);
        analysis.put("aboveMA20", aboveMA20);
        
        return analysis;
    }

    private Map<String, Object> analyzeRSI(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        double rsi = 20 + random.nextDouble() * 60;
        rsi = Math.round(rsi * 100) / 100.0;
        
        analysis.put("rsi", rsi);
        
        String status;
        String suggestion;
        
        if (rsi < 30) {
            status = "超卖";
            suggestion = "RSI处于超卖区域，可能存在反弹机会";
        } else if (rsi < 50) {
            status = "弱势";
            suggestion = "RSI处于弱势区域，短期走势偏弱";
        } else if (rsi < 70) {
            status = "强势";
            suggestion = "RSI处于强势区域，短期走势较好";
        } else {
            status = "超买";
            suggestion = "RSI处于超买区域，注意回调风险";
        }
        
        analysis.put("status", status);
        analysis.put("suggestion", suggestion);
        
        return analysis;
    }

    private Map<String, Object> analyzeVolume(StockRealtimeData stockData) {
        Map<String, Object> analysis = new HashMap<>();
        
        long currentVolume = stockData.getVolume() != null ? stockData.getVolume() : 0;
        long avgVolume = (long)(currentVolume * (0.7 + random.nextDouble() * 0.6));
        
        analysis.put("currentVolume", currentVolume);
        analysis.put("avgVolume", avgVolume);
        
        double volumeRatio = avgVolume > 0 ? (double) currentVolume / avgVolume : 1.0;
        volumeRatio = Math.round(volumeRatio * 100) / 100.0;
        analysis.put("volumeRatio", volumeRatio);
        
        String volumeStatus;
        String suggestion;
        
        if (volumeRatio > 2.0) {
            volumeStatus = "放量";
            suggestion = "成交量明显放大，市场关注度高，需结合股价走势判断";
        } else if (volumeRatio > 1.2) {
            volumeStatus = "温和放量";
            suggestion = "成交量温和放大，市场参与度提升";
        } else if (volumeRatio < 0.8) {
            volumeStatus = "缩量";
            suggestion = "成交量萎缩，市场观望情绪浓厚";
        } else {
            volumeStatus = "正常";
            suggestion = "成交量处于正常水平";
        }
        
        analysis.put("volumeStatus", volumeStatus);
        analysis.put("suggestion", suggestion);
        
        return analysis;
    }

    private List<Map<String, Object>> generatePriceHistory(StockRealtimeData stockData) {
        List<Map<String, Object>> history = new ArrayList<>();
        double basePrice = stockData.getCurrentPrice();
        
        for (int i = 20; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            double variation = (random.nextDouble() - 0.5) * 0.1;
            double price = basePrice * (1 + variation * (i / 10.0));
            price = Math.round(price * 100) / 100.0;
            
            dayData.put("day", i);
            dayData.put("price", price);
            dayData.put("volume", (long)(stockData.getVolume() * (0.5 + random.nextDouble())));
            history.add(dayData);
        }
        
        return history;
    }

    private String determineTrend(Map<String, Object> maAnalysis, 
                                  Map<String, Object> rsiAnalysis,
                                  Map<String, Object> volumeAnalysis) {
        String maTrend = (String) maAnalysis.get("maTrend");
        String rsiStatus = (String) rsiAnalysis.get("status");
        String volumeStatus = (String) volumeAnalysis.get("volumeStatus");
        
        int bullishSignals = 0;
        int bearishSignals = 0;
        
        if ("多头排列".equals(maTrend)) bullishSignals += 2;
        else if ("空头排列".equals(maTrend)) bearishSignals += 2;
        else if ("短期偏强".equals(maTrend)) bullishSignals += 1;
        
        if ("超卖".equals(rsiStatus)) bullishSignals += 1;
        else if ("超买".equals(rsiStatus)) bearishSignals += 1;
        else if ("强势".equals(rsiStatus)) bullishSignals += 1;
        else if ("弱势".equals(rsiStatus)) bearishSignals += 1;
        
        if ("放量".equals(volumeStatus) || "温和放量".equals(volumeStatus)) {
            if (bullishSignals > bearishSignals) bullishSignals += 1;
        }
        
        if (bullishSignals > bearishSignals + 1) return "上涨趋势";
        if (bearishSignals > bullishSignals + 1) return "下跌趋势";
        return "震荡趋势";
    }

    public Map<String, Object> generateSuggestion(String stockCode) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> fundamental = analyzeFundamental(stockCode);
        Map<String, Object> technical = analyzeTechnical(stockCode);
        
        if (!(Boolean) fundamental.getOrDefault("success", false) ||
            !(Boolean) technical.getOrDefault("success", false)) {
            result.put("success", false);
            result.put("message", "无法获取分析数据");
            return result;
        }
        
        result.put("success", true);
        result.put("stockCode", stockCode);
        result.put("stockName", fundamental.get("stockName"));
        result.put("currentPrice", fundamental.get("currentPrice"));

        String overallRating = (String) fundamental.get("overallRating");
        String trend = (String) technical.get("trend");
        
        String suggestion;
        String action;
        String color;
        
        if ("强烈推荐".equals(overallRating) && "上涨趋势".equals(trend)) {
            suggestion = "基本面优秀，技术面强势，建议积极买入";
            action = "买入";
            color = "green";
        } else if ("推荐".equals(overallRating) && ("上涨趋势".equals(trend) || "震荡趋势".equals(trend))) {
            suggestion = "基本面良好，技术面尚可，建议逢低买入";
            action = "买入";
            color = "green";
        } else if ("中性".equals(overallRating) && "上涨趋势".equals(trend)) {
            suggestion = "基本面一般，但技术面走强，可考虑短线参与";
            action = "持有";
            color = "yellow";
        } else if ("中性".equals(overallRating) && "震荡趋势".equals(trend)) {
            suggestion = "基本面和技术面均无明确方向，建议观望";
            action = "持有";
            color = "yellow";
        } else if ("谨慎".equals(overallRating) || "下跌趋势".equals(trend)) {
            suggestion = "风险较大，建议谨慎操作或暂时回避";
            action = "卖出";
            color = "red";
        } else {
            suggestion = "建议结合个人风险偏好做出决策";
            action = "持有";
            color = "yellow";
        }
        
        result.put("suggestion", suggestion);
        result.put("action", action);
        result.put("color", color);
        result.put("overallRating", overallRating);
        result.put("trend", trend);
        
        List<String> reasons = generateReasons(fundamental, technical);
        result.put("reasons", reasons);
        
        return result;
    }

    private List<String> generateReasons(Map<String, Object> fundamental, Map<String, Object> technical) {
        List<String> reasons = new ArrayList<>();
        
        Map<String, Object> peAnalysis = (Map<String, Object>) fundamental.get("peAnalysis");
        Map<String, Object> pbAnalysis = (Map<String, Object>) fundamental.get("pbAnalysis");
        Map<String, Object> industryAnalysis = (Map<String, Object>) fundamental.get("industryAnalysis");
        Map<String, Object> maAnalysis = (Map<String, Object>) technical.get("maAnalysis");
        Map<String, Object> rsiAnalysis = (Map<String, Object>) technical.get("rsiAnalysis");
        
        reasons.add("PE估值：" + peAnalysis.get("evaluation") + "（" + peAnalysis.get("pe") + "倍）");
        reasons.add("PB估值：" + pbAnalysis.get("evaluation") + "（" + pbAnalysis.get("pb") + "倍）");
        reasons.add("行业地位：" + industryAnalysis.get("position") + "（排名第" + industryAnalysis.get("industryRank") + "）");
        reasons.add("均线趋势：" + maAnalysis.get("maTrend"));
        reasons.add("RSI指标：" + rsiAnalysis.get("status") + "（" + rsiAnalysis.get("rsi") + "）");
        
        return reasons;
    }

    public Map<String, Object> analyzeTradeReason(String stockCode, String tradeType, 
                                                   Integer quantity, Double price) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> suggestion = generateSuggestion(stockCode);
        
        if (!(Boolean) suggestion.getOrDefault("success", false)) {
            result.put("success", false);
            result.put("message", "无法获取分析数据");
            return result;
        }
        
        result.put("success", true);
        result.put("stockCode", stockCode);
        result.put("stockName", suggestion.get("stockName"));
        result.put("tradeType", tradeType);
        result.put("quantity", quantity);
        result.put("price", price);
        result.put("totalAmount", Math.round(quantity * price * 100) / 100.0);
        
        List<String> tradeReasons = new ArrayList<>();
        String recommendedAction = (String) suggestion.get("action");
        String trend = (String) suggestion.get("trend");
        String overallRating = (String) suggestion.get("overallRating");
        
        if ("buy".equals(tradeType)) {
            tradeReasons.add("【买入理由分析】");
            
            if ("买入".equals(recommendedAction)) {
                tradeReasons.add("✓ 系统建议买入，当前时机较好");
            } else if ("卖出".equals(recommendedAction)) {
                tradeReasons.add("⚠ 系统建议卖出，当前买入存在风险");
            } else {
                tradeReasons.add("○ 系统建议持有，买入需谨慎");
            }
            
            tradeReasons.add("基本面评级：" + overallRating);
            tradeReasons.add("技术面趋势：" + trend);
            
            List<String> analysisReasons = (List<String>) suggestion.get("reasons");
            tradeReasons.addAll(analysisReasons);
            
        } else if ("sell".equals(tradeType)) {
            tradeReasons.add("【卖出理由分析】");
            
            Double currentPrice = (Double) suggestion.get("currentPrice");
            double profitPercent = ((price - currentPrice) / currentPrice) * 100;
            
            tradeReasons.add("买入价格：¥" + currentPrice);
            tradeReasons.add("卖出价格：¥" + price);
            tradeReasons.add("盈亏比例：" + String.format("%.2f", profitPercent) + "%");
            
            if (profitPercent > 10) {
                tradeReasons.add("✓ 已获得较好收益，可考虑获利了结");
            } else if (profitPercent > 0) {
                tradeReasons.add("○ 小幅盈利，可根据个人判断决定是否卖出");
            } else if (profitPercent > -10) {
                tradeReasons.add("○ 小幅亏损，可考虑止损或继续持有");
            } else {
                tradeReasons.add("⚠ 亏损较大，建议评估是否止损");
            }
            
            if ("卖出".equals(recommendedAction)) {
                tradeReasons.add("✓ 系统建议卖出，当前时机较好");
            }
        }
        
        result.put("tradeReasons", tradeReasons);
        
        return result;
    }
}
