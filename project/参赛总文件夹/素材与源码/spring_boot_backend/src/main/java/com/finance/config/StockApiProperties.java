package com.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stock.api")
public class StockApiProperties {

    private SinaConfig sina = new SinaConfig();
    private EastmoneyConfig eastmoney = new EastmoneyConfig();
    private CacheConfig cache = new CacheConfig();

    public SinaConfig getSina() {
        return sina;
    }

    public void setSina(SinaConfig sina) {
        this.sina = sina;
    }

    public EastmoneyConfig getEastmoney() {
        return eastmoney;
    }

    public void setEastmoney(EastmoneyConfig eastmoney) {
        this.eastmoney = eastmoney;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    public static class SinaConfig {
        private String url = "https://hq.sinajs.cn/list=";
        private boolean enabled = true;
        private int timeout = 5000;
        private int retryTimes = 3;
        private int retryInterval = 1000;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
        }

        public int getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(int retryInterval) {
            this.retryInterval = retryInterval;
        }
    }

    public static class EastmoneyConfig {
        private String url = "https://push2.eastmoney.com/api/qt/stock/get";
        private boolean enabled = false;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class CacheConfig {
        private boolean enabled = true;
        private int expireSeconds = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getExpireSeconds() {
            return expireSeconds;
        }

        public void setExpireSeconds(int expireSeconds) {
            this.expireSeconds = expireSeconds;
        }
    }
}
