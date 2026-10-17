package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("badge")
public class Badge {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private String iconUrl;
    private String conditionType;
    private Integer conditionValue;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public Integer getConditionValue() { return conditionValue; }
    public void setConditionValue(Integer conditionValue) { this.conditionValue = conditionValue; }
}
