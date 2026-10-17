package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.StockRealtimeData;
import com.finance.entity.UserRiskProfile;
import com.finance.entity.UserStockHold;
import com.finance.mapper.UserRiskProfileMapper;
import com.finance.mapper.UserStockHoldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RiskWarningService {

    @Autowired
    private UserRiskProfileMapper userRiskProfileMapper;

    @Autowired
    private UserStockHoldMapper userStockHoldMapper;

    @Autowired
    private StockDataService stockDataService;

    private static final Map<String, Integer> RISK_TYPE_SCORE = new LinkedHashMap<>();
    static {
        RISK_TYPE_SCORE.put("保守型", 1);
        RISK_TYPE_SCORE.put("稳健型", 2);
        RISK_TYPE_SCORE.put("平衡型", 3);
        RISK_TYPE_SCORE.put("成长型", 4);
        RISK_TYPE_SCORE.put("进取型", 5);
    }

    private static final Set<String> BLUE_CHIP_PREFIXES = new HashSet<>(Arrays.asList(
        "SH600000", "SH600519", "SH601318", "SH600036", "SH601398", "SH601939",
        "SH600030", "SH600028", "SH601288", "SH600016", "SZ000001", "SZ000002",
        "SZ000333", "SZ000651", "SZ000858", "SZ002415", "SZ002594"
    ));

    public Map<String, Object> getUserRiskProfile(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserRiskProfile profile = userRiskProfileMapper.selectOne(
                new LambdaQueryWrapper<UserRiskProfile>().eq(UserRiskProfile::getUserId, userId)
            );
            if (profile != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", profile.getId());
                data.put("userId", profile.getUserId());
                data.put("riskType", profile.getRiskType());
                data.put("riskScore", profile.getRiskScore());
                data.put("riskLevel", getRiskLevel(profile.getRiskType()));
                data.put("createdAt", profile.getCreatedAt());
                data.put("updatedAt", profile.getUpdatedAt());
                result.put("data", data);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "用户风险偏好未设置");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> setUserRiskProfile(Integer userId, String riskType, Integer riskScore) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!RISK_TYPE_SCORE.containsKey(riskType)) {
                result.put("success", false);
                result.put("error", "无效的风险类型");
                return result;
            }

            UserRiskProfile profile = userRiskProfileMapper.selectOne(
                new LambdaQueryWrapper<UserRiskProfile>().eq(UserRiskProfile::getUserId, userId)
            );

            LocalDateTime now = LocalDateTime.now();
            if (profile == null) {
                profile = new UserRiskProfile();
                profile.setUserId(userId);
                profile.setRiskType(riskType);
                profile.setRiskScore(riskScore != null ? riskScore : RISK_TYPE_SCORE.get(riskType));
                profile.setCreatedAt(now);
                profile.setUpdatedAt(now);
                userRiskProfileMapper.insert(profile);
            } else {
                profile.setRiskType(riskType);
                profile.setRiskScore(riskScore != null ? riskScore : RISK_TYPE_SCORE.get(riskType));
                profile.setUpdatedAt(now);
                userRiskProfileMapper.updateById(profile);
            }

            result.put("data", Map.of(
                "userId", userId,
                "riskType", riskType,
                "riskScore", profile.getRiskScore()
            ));
            result.put("success", true);
            result.put("message", "风险偏好设置成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> checkStockRiskMatch(Integer userId, String stockCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserRiskProfile profile = userRiskProfileMapper.selectOne(
                new LambdaQueryWrapper<UserRiskProfile>().eq(UserRiskProfile::getUserId, userId)
            );

            if (profile == null) {
                result.put("success", false);
                result.put("error", "请先设置风险偏好");
                return result;
            }

            String stockRiskLevel = getStockRiskLevel(stockCode);
            int userRiskLevel = profile.getRiskScore();
            int stockRiskScore = getStockRiskScore(stockRiskLevel);

            boolean isMatch = userRiskLevel >= stockRiskScore;
            String warningMessage = null;

            if (!isMatch) {
                warningMessage = String.format(
                    "风险提示：该股票属于【%s】，您的风险偏好为【%s】，可能超出您的风险承受能力",
                    stockRiskLevel, profile.getRiskType()
                );
            }

            Map<String, Object> data = new HashMap<>();
            data.put("stockCode", stockCode);
            data.put("stockRiskLevel", stockRiskLevel);
            data.put("stockRiskScore", stockRiskScore);
            data.put("userRiskType", profile.getRiskType());
            data.put("userRiskScore", userRiskLevel);
            data.put("isMatch", isMatch);
            data.put("warningMessage", warningMessage);

            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> checkPortfolioRisk(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> warnings = new ArrayList<>();

        try {
            List<UserStockHold> holds = userStockHoldMapper.selectList(
                new LambdaQueryWrapper<UserStockHold>().eq(UserStockHold::getUserId, userId)
            );

            if (holds.isEmpty()) {
                result.put("data", Map.of("warnings", warnings, "hasWarning", false));
                result.put("success", true);
                return result;
            }

            Map<String, StockRealtimeData> stockDataMap = new HashMap<>();
            BigDecimal totalValue = BigDecimal.ZERO;

            for (UserStockHold hold : holds) {
                StockRealtimeData stockData = stockDataService.getRealtimeData(hold.getStockCode());
                if (stockData != null) {
                    stockDataMap.put(hold.getStockCode(), stockData);
                    BigDecimal currentValue = stockData.getCurrentPrice() != null
                        ? new BigDecimal(stockData.getCurrentPrice().toString())
                            .multiply(new BigDecimal(hold.getHoldNum()))
                        : BigDecimal.ZERO;
                    totalValue = totalValue.add(currentValue);
                }
            }

            for (UserStockHold hold : holds) {
                StockRealtimeData stockData = stockDataMap.get(hold.getStockCode());
                if (stockData == null) continue;

                if (stockData.getChangePercent() != null) {
                    double changePercent = Math.abs(stockData.getChangePercent());
                    if (changePercent >= 5) {
                        Map<String, Object> warning = new HashMap<>();
                        warning.put("type", "PRICE_CHANGE");
                        warning.put("level", changePercent >= 9 ? "HIGH" : "MEDIUM");
                        warning.put("stockCode", hold.getStockCode());
                        warning.put("stockName", hold.getStockName());
                        warning.put("message", String.format(
                            "【%s】今日涨跌幅 %.2f%%，波动较大，请注意风险",
                            hold.getStockName(), stockData.getChangePercent()
                        ));
                        warning.put("changePercent", stockData.getChangePercent());
                        warnings.add(warning);
                    }
                }

                if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal currentValue = stockData.getCurrentPrice() != null
                        ? new BigDecimal(stockData.getCurrentPrice().toString())
                            .multiply(new BigDecimal(hold.getHoldNum()))
                        : BigDecimal.ZERO;
                    BigDecimal ratio = currentValue.multiply(new BigDecimal("100"))
                        .divide(totalValue, 2, RoundingMode.HALF_UP);

                    if (ratio.compareTo(new BigDecimal("30")) > 0) {
                        Map<String, Object> warning = new HashMap<>();
                        warning.put("type", "CONCENTRATION");
                        warning.put("level", ratio.compareTo(new BigDecimal("50")) > 0 ? "HIGH" : "MEDIUM");
                        warning.put("stockCode", hold.getStockCode());
                        warning.put("stockName", hold.getStockName());
                        warning.put("message", String.format(
                            "【%s】持仓占比 %.2f%%，集中度过高，建议分散投资",
                            hold.getStockName(), ratio
                        ));
                        warning.put("concentrationRatio", ratio);
                        warnings.add(warning);
                    }
                }
            }

            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            for (UserStockHold hold : holds) {
                StockRealtimeData stockData = stockDataMap.get(hold.getStockCode());
                if (stockData != null && stockData.getCurrentPrice() != null) {
                    BigDecimal currentValue = new BigDecimal(stockData.getCurrentPrice().toString())
                        .multiply(new BigDecimal(hold.getHoldNum()));
                    BigDecimal costValue = hold.getBuyPrice()
                        .multiply(new BigDecimal(hold.getHoldNum()));
                    totalProfit = totalProfit.add(currentValue.subtract(costValue));
                    totalCost = totalCost.add(costValue);
                }
            }

            if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitRate = totalProfit.multiply(new BigDecimal("100"))
                    .divide(totalCost, 2, RoundingMode.HALF_UP);
                double absProfitRate = Math.abs(profitRate.doubleValue());

                if (absProfitRate >= 15) {
                    Map<String, Object> warning = new HashMap<>();
                    warning.put("type", "PORTFOLIO_VOLATILITY");
                    warning.put("level", absProfitRate >= 25 ? "HIGH" : "MEDIUM");
                    warning.put("message", String.format(
                        "整体持仓%s %.2f%%，波动较大，请注意风险控制",
                        profitRate.compareTo(BigDecimal.ZERO) >= 0 ? "盈利" : "亏损",
                        Math.abs(profitRate.doubleValue())
                    ));
                    warning.put("profitRate", profitRate);
                    warnings.add(warning);
                }
            }

            result.put("data", Map.of(
                "warnings", warnings,
                "hasWarning", !warnings.isEmpty(),
                "warningCount", warnings.size()
            ));
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private String getStockRiskLevel(String stockCode) {
        if (stockCode == null) return "中风险";

        String upperCode = stockCode.toUpperCase();

        if (BLUE_CHIP_PREFIXES.stream().anyMatch(upperCode::startsWith)) {
            return "低风险";
        }

        if (upperCode.startsWith("SH688") || upperCode.startsWith("SZ30")) {
            return "高风险";
        }

        if (upperCode.contains("ST") || upperCode.contains("*ST")) {
            return "高风险";
        }

        return "中风险";
    }

    private int getStockRiskScore(String riskLevel) {
        switch (riskLevel) {
            case "低风险": return 1;
            case "中风险": return 3;
            case "高风险": return 5;
            default: return 3;
        }
    }

    private int getRiskLevel(String riskType) {
        return RISK_TYPE_SCORE.getOrDefault(riskType, 3);
    }

    public List<Map<String, Object>> getRiskTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        RISK_TYPE_SCORE.forEach((type, score) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("type", type);
            item.put("score", score);
            item.put("description", getRiskTypeDescription(type));
            types.add(item);
        });
        return types;
    }

    private String getRiskTypeDescription(String type) {
        switch (type) {
            case "保守型": return "追求稳定收益，风险承受能力较低";
            case "稳健型": return "追求稳健增长，可接受小幅波动";
            case "平衡型": return "追求收益与风险平衡";
            case "成长型": return "追求较高收益，可承受较大波动";
            case "进取型": return "追求高收益，可承受高风险";
            default: return "";
        }
    }
}
