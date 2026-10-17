package com.finance.entity;

public class StockRealtimeData {

    private String code;
    private String name;
    private Double openPrice;
    private Double preClosePrice;
    private Double currentPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double bidPrice;
    private Double askPrice;
    private Long volume;
    private Double amount;
    private Long bidVolume;
    private Long askVolume;
    private String date;
    private String time;
    private Double changeAmount;
    private Double changePercent;

    public StockRealtimeData() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Double openPrice) {
        this.openPrice = openPrice;
    }

    public Double getPreClosePrice() {
        return preClosePrice;
    }

    public void setPreClosePrice(Double preClosePrice) {
        this.preClosePrice = preClosePrice;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Double highPrice) {
        this.highPrice = highPrice;
    }

    public Double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getBidVolume() {
        return bidVolume;
    }

    public void setBidVolume(Long bidVolume) {
        this.bidVolume = bidVolume;
    }

    public Long getAskVolume() {
        return askVolume;
    }

    public void setAskVolume(Long askVolume) {
        this.askVolume = askVolume;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }
}
