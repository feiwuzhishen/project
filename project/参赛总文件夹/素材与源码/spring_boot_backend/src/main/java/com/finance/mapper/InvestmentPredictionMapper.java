package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.InvestmentPrediction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface InvestmentPredictionMapper extends BaseMapper<InvestmentPrediction> {

    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN is_accurate = true THEN 1 ELSE 0 END) as accurate " +
            "FROM investment_prediction " +
            "WHERE user_id = #{userId} AND is_accurate IS NOT NULL")
    Map<String, Object> countAccuracyByUserId(Integer userId);

    @Select("SELECT prediction_type, COUNT(*) as count, " +
            "SUM(CASE WHEN is_accurate = true THEN 1 ELSE 0 END) as accurate " +
            "FROM investment_prediction " +
            "WHERE user_id = #{userId} AND is_accurate IS NOT NULL " +
            "GROUP BY prediction_type")
    List<Map<String, Object>> countAccuracyByType(Integer userId);
}
