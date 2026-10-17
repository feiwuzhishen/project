package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user_honor")
public class UserHonor {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer honorValue;
    private String honorLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getHonorValue() { return honorValue; }
    public void setHonorValue(Integer honorValue) { this.honorValue = honorValue; }
    public String getHonorLevel() { return honorLevel; }
    public void setHonorLevel(String honorLevel) { this.honorLevel = honorLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
