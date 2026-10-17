package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user_asset")
public class UserAsset {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer stockRatio;
    private Integer fundRatio;
    private Integer bondRatio;
    private Integer etfRatio;
    private Integer derivativesRatio;
    private Integer cashRatio;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getStockRatio() { return stockRatio; }
    public void setStockRatio(Integer stockRatio) { this.stockRatio = stockRatio; }
    public Integer getFundRatio() { return fundRatio; }
    public void setFundRatio(Integer fundRatio) { this.fundRatio = fundRatio; }
    public Integer getBondRatio() { return bondRatio; }
    public void setBondRatio(Integer bondRatio) { this.bondRatio = bondRatio; }
    public Integer getEtfRatio() { return etfRatio; }
    public void setEtfRatio(Integer etfRatio) { this.etfRatio = etfRatio; }
    public Integer getDerivativesRatio() { return derivativesRatio; }
    public void setDerivativesRatio(Integer derivativesRatio) { this.derivativesRatio = derivativesRatio; }
    public Integer getCashRatio() { return cashRatio; }
    public void setCashRatio(Integer cashRatio) { this.cashRatio = cashRatio; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
