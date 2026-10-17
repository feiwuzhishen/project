package com.finance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RealDataService {

    private final RestTemplate restTemplate;

    public RealDataService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public Map<String, Object> getTopCoins() {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = "https://api.binance.com/api/v3/ticker/24hr";
            List<Map<String, Object>> binanceData = restTemplate.getForObject(url, List.class);

            List<Map<String, Object>> filteredData = new ArrayList<>();
            String[] targetSymbols = {"BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
                                       "ADAUSDT", "AVAXUSDT", "DOGEUSDT", "DOTUSDT", "LINKUSDT"};

            if (binanceData != null) {
                for (String symbol : targetSymbols) {
                    for (Map<String, Object> item : binanceData) {
                        if (symbol.equals(item.get("symbol"))) {
                            Map<String, Object> coin = new HashMap<>();
                            coin.put("id", symbol.toLowerCase().replace("usdt", ""));
                            coin.put("symbol", symbol.replace("USDT", "").toLowerCase());
                            coin.put("name", getCoinName(symbol));

                            Object lastPrice = item.get("lastPrice");
                            Object priceChangePercent = item.get("priceChangePercent");
                            Object quoteVolume = item.get("quoteVolume");

                            coin.put("currentPrice", parseDouble(lastPrice));
                            coin.put("priceChangePercent", parseDouble(priceChangePercent));
                            coin.put("volume", parseDouble(quoteVolume));

                            filteredData.add(coin);
                            break;
                        }
                    }
                }
            }

            result.put("data", filteredData);
            result.put("success", true);
            result.put("count", filteredData.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "无法获取数据: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        return result;
    }

    public Map<String, Object> getCryptoData() {
        return getTopCoins();
    }

    public Map<String, Object> getGlobalMarketData() {
        Map<String, Object> result = new HashMap<>();
        try {
            String btcUrl = "https://api.binance.com/api/v3/ticker/24hr?symbol=BTCUSDT";
            Map<String, Object> btcData = restTemplate.getForObject(btcUrl, Map.class);

            double btcDominance = 52.5;
            double totalMarketCap = 3500000000000L;
            double priceChange = 0;

            if (btcData != null) {
                Object quoteVol = btcData.get("quoteVolume");
                Object changePercent = btcData.get("priceChangePercent");
                Object lastPrice = btcData.get("lastPrice");

                if (quoteVol != null) {
                    double btcVolume = parseDouble(quoteVol);
                    totalMarketCap = btcVolume / (btcDominance / 100) * 1000;
                }
                if (changePercent != null) {
                    priceChange = parseDouble(changePercent);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("total_market_cap", Map.of("usd", totalMarketCap));
            data.put("market_cap_change_percentage_24h_usd", priceChange);
            data.put("market_cap_percentage", Map.of("btc", btcDominance, "eth", 17.5));

            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "无法获取全球市场数据: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getForexData() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> forexList = new ArrayList<>();

        try {
            String[] pairs = {"BTCUSDT", "ETHUSDT", "BNBUSDT"};
            for (String pair : pairs) {
                String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + pair;
                Map<String, Object> ticker = restTemplate.getForObject(url, Map.class);

                if (ticker != null && ticker.get("price") != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("currency", pair.replace("USDT", ""));
                    item.put("value", parseDouble(ticker.get("price")));
                    item.put("name", getCoinName(pair));
                    forexList.add(item);
                }
            }
        } catch (Exception e) {
            // ignore errors, return what we have
        }

        if (forexList.isEmpty()) {
            Map<String, Object> btc = new HashMap<>();
            btc.put("currency", "BTC");
            btc.put("value", 1.0);
            btc.put("name", "Bitcoin");
            forexList.add(btc);
        }

        result.put("data", forexList);
        result.put("success", true);
        return result;
    }

    public Map<String, Object> getExchangeRates() {
        return getForexData();
    }

    public Map<String, Object> getStockData(String symbol) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "股票功能已禁用");
        return result;
    }

    public Map<String, Object> testApi() {
        Map<String, Object> result = new HashMap<>();
        try {
            String testUrl = "https://api.binance.com/api/v3/ping";
            Object response = restTemplate.getForObject(testUrl, Object.class);
            result.put("success", true);
            result.put("message", "API连接成功");
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "API连接失败: " + e.getMessage());
        }
        return result;
    }

    private double parseDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String getCoinName(String symbol) {
        Map<String, String> names = new HashMap<>();
        names.put("BTCUSDT", "Bitcoin");
        names.put("ETHUSDT", "Ethereum");
        names.put("BNBUSDT", "Binance Coin");
        names.put("SOLUSDT", "Solana");
        names.put("XRPUSDT", "XRP");
        names.put("ADAUSDT", "Cardano");
        names.put("AVAXUSDT", "Avalanche");
        names.put("DOGEUSDT", "Dogecoin");
        names.put("DOTUSDT", "Polkadot");
        names.put("LINKUSDT", "Chainlink");
        return names.getOrDefault(symbol, symbol.replace("USDT", ""));
    }
}
