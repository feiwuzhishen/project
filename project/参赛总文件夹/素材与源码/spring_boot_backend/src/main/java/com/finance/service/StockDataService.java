package com.finance.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.finance.config.StockApiProperties;
import com.finance.entity.StockRealtimeData;

@Service
public class StockDataService {

    private static final Logger logger = LoggerFactory.getLogger(StockDataService.class);

    private final RestTemplate restTemplate;
    private final StockApiProperties stockApiProperties;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public StockDataService(StockApiProperties stockApiProperties) {
        this.stockApiProperties = stockApiProperties;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
            .add(0, new StringHttpMessageConverter(Charset.forName("GB18030")));
    }

    public StockRealtimeData getRealtimeData(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        String normalizedCode = normalizeCode(code);
        String cacheKey = "stock_" + normalizedCode;

        if (stockApiProperties.getCache().isEnabled()) {
            CacheEntry cached = cache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                return cached.getData();
            }
        }

        String response = fetchFromSinaApi(normalizedCode);
        if (response == null || response.isEmpty()) {
            return null;
        }

        StockRealtimeData data = parseSinaResponse(normalizedCode, response);
        if (data != null && stockApiProperties.getCache().isEnabled()) {
            cache.put(cacheKey, new CacheEntry(data, stockApiProperties.getCache().getExpireSeconds()));
        }

        return data;
    }

    public List<StockRealtimeData> getRealtimeDataList(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }

        List<StockRealtimeData> result = new ArrayList<>();
        List<String> uncachedCodes = new ArrayList<>();
        Map<String, StockRealtimeData> cachedData = new HashMap<>();

        for (String code : codes) {
            String normalizedCode = normalizeCode(code);
            String cacheKey = "stock_" + normalizedCode;

            if (stockApiProperties.getCache().isEnabled()) {
                CacheEntry cached = cache.get(cacheKey);
                if (cached != null && !cached.isExpired()) {
                    cachedData.put(normalizedCode, cached.getData());
                    continue;
                }
            }
            uncachedCodes.add(normalizedCode);
        }

        if (!uncachedCodes.isEmpty()) {
            String codesParam = String.join(",", uncachedCodes);
            String response = fetchFromSinaApi(codesParam);
            if (response != null && !response.isEmpty()) {
                Map<String, StockRealtimeData> fetchedData = parseSinaBatchResponse(response);
                for (String code : uncachedCodes) {
                    StockRealtimeData data = fetchedData.get(code);
                    if (data != null) {
                        result.add(data);
                        if (stockApiProperties.getCache().isEnabled()) {
                            cache.put("stock_" + code, new CacheEntry(data, stockApiProperties.getCache().getExpireSeconds()));
                        }
                    }
                }
            }
        }

        for (String code : codes) {
            String normalizedCode = normalizeCode(code);
            StockRealtimeData cached = cachedData.get(normalizedCode);
            if (cached != null) {
                result.add(cached);
            }
        }

        return result;
    }

    public List<StockRealtimeData> searchStock(String keyword) {
        List<StockRealtimeData> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result;
        }

        Map<String, String> stockMap = getStockCodeMap();
        String lowerKeyword = keyword.toLowerCase();

        for (Map.Entry<String, String> entry : stockMap.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lowerKeyword) ||
                entry.getValue().toLowerCase().contains(lowerKeyword)) {
                StockRealtimeData data = getRealtimeData(entry.getKey());
                if (data != null) {
                    result.add(data);
                }
            }
        }

        return result;
    }

    private String fetchFromSinaApi(String codes) {
        if (!stockApiProperties.getSina().isEnabled()) {
            logger.warn("新浪财经API已禁用");
            return null;
        }

        String urlStr = stockApiProperties.getSina().getUrl() + codes;
        int retryTimes = stockApiProperties.getSina().getRetryTimes();
        int retryInterval = stockApiProperties.getSina().getRetryInterval();

        for (int i = 0; i <= retryTimes; i++) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Referer", "https://finance.sina.com.cn");
                connection.setConnectTimeout(stockApiProperties.getSina().getTimeout());
                connection.setReadTimeout(stockApiProperties.getSina().getTimeout());

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new RuntimeException("HTTP error: " + responseCode);
                }

                InputStream inputStream = connection.getInputStream();
                String contentType = connection.getContentType();
                String charset = "GBK";
                if (contentType != null && contentType.contains("charset=")) {
                    String ctCharset = contentType.substring(contentType.indexOf("charset=") + 8);
                    if (ctCharset.contains(";")) {
                        ctCharset = ctCharset.substring(0, ctCharset.indexOf(";"));
                    }
                    charset = ctCharset.trim();
                }
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName(charset)));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String result = response.toString();
                return result;
            } catch (Exception e) {
                logger.warn("调用新浪财经API失败，第{}次重试: {}", i + 1, e.getMessage());
                if (i < retryTimes) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        logger.error("调用新浪财经API失败，已达到最大重试次数");
        return null;
    }

    private StockRealtimeData parseSinaResponse(String code, String response) {
        try {
            String varName = "var hq_str_" + code + "=\"";
            int startIndex = response.indexOf(varName);
            if (startIndex == -1) {
                return null;
            }

            startIndex += varName.length();
            int endIndex = response.indexOf("\";", startIndex);
            if (endIndex == -1) {
                return null;
            }

            String dataStr = response.substring(startIndex, endIndex);
            if (dataStr.isEmpty()) {
                return null;
            }

            return parseStockData(code, dataStr);
        } catch (Exception e) {
            logger.error("解析新浪财经响应失败: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, StockRealtimeData> parseSinaBatchResponse(String response) {
        Map<String, StockRealtimeData> result = new HashMap<>();

        String[] lines = response.split("var hq_str_");
        for (String line : lines) {
            if (line.isEmpty()) continue;

            int codeEnd = line.indexOf("=\"");
            if (codeEnd == -1) continue;

            String code = line.substring(0, codeEnd);
            int dataStart = codeEnd + 2;
            int dataEnd = line.indexOf("\";", dataStart);
            if (dataEnd == -1) continue;

            String dataStr = line.substring(dataStart, dataEnd);
            if (dataStr.isEmpty()) continue;

            StockRealtimeData data = parseStockData(code, dataStr);
            if (data != null) {
                result.put(code, data);
            }
        }

        return result;
    }

    private StockRealtimeData parseStockData(String code, String dataStr) {
        String[] parts = dataStr.split(",");
        if (parts.length < 32) {
            return null;
        }

        StockRealtimeData data = new StockRealtimeData();
        data.setCode(code);
        data.setName(parts[0]);
        data.setOpenPrice(parseDouble(parts[1]));
        data.setPreClosePrice(parseDouble(parts[2]));
        data.setCurrentPrice(parseDouble(parts[3]));
        data.setHighPrice(parseDouble(parts[4]));
        data.setLowPrice(parseDouble(parts[5]));
        data.setBidPrice(parseDouble(parts[6]));
        data.setAskPrice(parseDouble(parts[7]));
        data.setVolume(parseLong(parts[8]));
        data.setAmount(parseDouble(parts[9]));
        data.setBidVolume(parseLong(parts[10]));
        data.setAskVolume(parseLong(parts[20]));
        data.setDate(parts[30]);
        data.setTime(parts[31]);

        if (data.getPreClosePrice() != null && data.getPreClosePrice() > 0 && data.getCurrentPrice() != null) {
            double change = data.getCurrentPrice() - data.getPreClosePrice();
            double changePercent = (change / data.getPreClosePrice()) * 100;
            data.setChangeAmount(Math.round(change * 100) / 100.0);
            data.setChangePercent(Math.round(changePercent * 100) / 100.0);
        }

        return data;
    }

    private String normalizeCode(String code) {
        if (code == null) return "";
        code = code.trim().toLowerCase();
        if (code.startsWith("sh") || code.startsWith("sz")) {
            return code;
        }
        if (code.startsWith("6")) {
            return "sh" + code;
        }
        if (code.startsWith("0") || code.startsWith("3")) {
            return "sz" + code;
        }
        return code;
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private long parseLong(String value) {
        if (value == null || value.isEmpty()) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Map<String, String> getStockCodeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("SH600000", "浦发银行");
        map.put("SH600001", "邯郸钢铁");
        map.put("SH600002", "齐鲁石化");
        map.put("SH600003", "ST东北高");
        map.put("SH600004", "白云机场");
        map.put("SH600005", "武钢股份");
        map.put("SH600006", "东风汽车");
        map.put("SH600007", "中国国贸");
        map.put("SH600008", "首创股份");
        map.put("SH600009", "上海机场");
        map.put("SH600010", "包钢股份");
        map.put("SH600011", "华能国际");
        map.put("SH600012", "皖通高速");
        map.put("SH600015", "华夏银行");
        map.put("SH600016", "民生银行");
        map.put("SH600017", "日照港");
        map.put("SH600018", "上港集团");
        map.put("SH600019", "宝钢股份");
        map.put("SH600020", "中原高速");
        map.put("SH600028", "中国石化");
        map.put("SH600029", "南方航空");
        map.put("SH600030", "中信证券");
        map.put("SH600031", "三一重工");
        map.put("SH600033", "福建高速");
        map.put("SH600035", "楚天高速");
        map.put("SH600036", "招商银行");
        map.put("SH600037", "歌华有线");
        map.put("SH600038", "哈飞股份");
        map.put("SH600039", "四川路桥");
        map.put("SH600048", "保利发展");
        map.put("SH600050", "中国联通");
        map.put("SZ000001", "平安银行");
        map.put("SZ000002", "万科A");
        map.put("SZ000004", "国华网安");
        map.put("SZ000005", "ST星源");
        map.put("SZ000006", "深振业A");
        map.put("SZ000007", "全新好");
        map.put("SZ000008", "神州高铁");
        map.put("SZ000009", "中国宝安");
        map.put("SZ000010", "美丽生态");
        map.put("SZ000011", "深物业A");
        map.put("SZ000012", "南玻A");
        map.put("SZ000014", "沙河股份");
        map.put("SZ000016", "深康佳A");
        map.put("SZ000017", "深中华A");
        map.put("SZ000019", "深粮控股");
        map.put("SZ000020", "深华发A");
        map.put("SZ000021", "深科技");
        map.put("SZ000023", "深天地A");
        map.put("SZ000025", "特力A");
        map.put("SZ000026", "飞亚达");
        map.put("SZ000027", "深能源A");
        map.put("SZ000028", "国药一致");
        map.put("SZ000029", "深深房A");
        map.put("SZ000030", "富奥股份");
        map.put("SZ000031", "大悦城");
        map.put("SZ000032", "深桑达A");
        map.put("SZ000033", "深石化A");
        map.put("SZ000034", "神州数码");
        map.put("SZ000035", "中国天楹");
        map.put("SZ000036", "华联控股");
        map.put("SZ000037", "深南电A");
        map.put("SZ000038", "深大通");
        map.put("SZ000039", "中集集团");
        map.put("SZ000040", "东旭蓝天");
        map.put("SZ000042", "中洲控股");
        map.put("SZ000043", "中航地产");
        map.put("SZ000045", "深纺织A");
        map.put("SZ000046", "泛海控股");
        map.put("SZ000048", "康达尔");
        map.put("SZ000049", "德赛电池");
        map.put("SZ000050", "深天马A");
        map.put("SZ000055", "方大集团");
        map.put("SZ000056", "国贸科技");
        map.put("SZ000058", "深赛格");
        map.put("SZ000059", "华锦股份");
        map.put("SZ000060", "中金岭南");
        map.put("SZ000061", "农产品");
        map.put("SZ000062", "深圳华强");
        map.put("SZ000063", "中兴通讯");
        map.put("SZ000066", "中国长城");
        map.put("SZ000068", "华控赛格");
        map.put("SZ000069", "华侨城A");
        map.put("SZ000070", "特发信息");
        map.put("SZ000078", "海王生物");
        map.put("SZ000088", "盐田港");
        map.put("SZ000089", "深圳机场");
        map.put("SZ000090", "天健集团");
        map.put("SZ000099", "中信海直");
        map.put("SZ000100", "TCL科技");
        map.put("SZ000155", "川能动力");
        map.put("SZ000333", "美的集团");
        map.put("SZ000338", "潍柴动力");
        map.put("SZ000425", "徐工机械");
        map.put("SZ000538", "云南白药");
        map.put("SZ000568", "泸州老窖");
        map.put("SZ000625", "长安汽车");
        map.put("SZ000651", "格力电器");
        map.put("SZ000661", "长春高新");
        map.put("SZ000703", "世纪华通");
        map.put("SZ000725", "京东方A");
        map.put("SZ000768", "中航西飞");
        map.put("SZ000776", "广发证券");
        map.put("SZ000858", "五粮液");
        map.put("SZ000876", "新希望");
        map.put("SZ000938", "紫光股份");
        map.put("SZ002001", "新和成");
        map.put("SZ002007", "华兰生物");
        map.put("SZ002008", "大族激光");
        map.put("SZ002024", "苏宁易购");
        map.put("SZ002027", "分众传媒");
        map.put("SZ002049", "紫光国微");
        map.put("SZ002050", "三花智控");
        map.put("SZ002120", "韵达股份");
        map.put("SZ002142", "宁波银行");
        map.put("SZ002230", "科大讯飞");
        map.put("SZ002236", "大华股份");
        map.put("SZ002241", "歌尔股份");
        map.put("SZ002304", "洋河股份");
        map.put("SZ002311", "海大集团");
        map.put("SZ002352", "顺丰控股");
        map.put("SZ002410", "广联达");
        map.put("SZ002415", "海康威视");
        map.put("SZ002475", "立讯精密");
        map.put("SZ002594", "比亚迪");
        map.put("SZ002601", "龙蟒佰利");
        map.put("SZ002603", "以岭药业");
        map.put("SZ002714", "牧原股份");
        map.put("SZ002812", "恩捷股份");
        map.put("SZ002841", "视源股份");
        map.put("SZ003001", "中岩大地");
        return map;
    }

    private static class CacheEntry {
        private final StockRealtimeData data;
        private final long expireTime;

        public CacheEntry(StockRealtimeData data, int expireSeconds) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + expireSeconds * 1000L;
        }

        public StockRealtimeData getData() {
            return data;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
