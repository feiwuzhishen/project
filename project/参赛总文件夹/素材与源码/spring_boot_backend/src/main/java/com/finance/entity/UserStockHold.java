package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("user_stock_hold")
public class UserStockHold {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String stockCode;
    private String stockName;
    private Integer holdNum;
    private BigDecimal buyPrice;
    private LocalDateTime holdTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    public Integer getHoldNum() { return holdNum; }
    public void setHoldNum(Integer holdNum) { this.holdNum = holdNum; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public void setBuyPrice(BigDecimal buyPrice) { this.buyPrice = buyPrice; }
    public LocalDateTime getHoldTime() { return holdTime; }
    public void setHoldTime(LocalDateTime holdTime) { this.holdTime = holdTime; }
}
