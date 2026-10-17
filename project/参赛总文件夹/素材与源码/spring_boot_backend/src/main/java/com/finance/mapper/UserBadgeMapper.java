package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.UserBadge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserBadgeMapper extends BaseMapper<UserBadge> {
    
    @Select("SELECT ub.*, b.name, b.description, b.icon_url, b.condition_type, b.condition_value " +
            "FROM user_badge ub " +
            "JOIN badge b ON ub.badge_id = b.id " +
            "WHERE ub.user_id = #{userId}")
    List<Map<String, Object>> selectUserBadgesWithInfo(Integer userId);
    
    @Select("SELECT u.id as user_id, u.username, u.nickname, COUNT(ub.id) as badge_count " +
            "FROM user u " +
            "LEFT JOIN user_badge ub ON u.id = ub.user_id " +
            "GROUP BY u.id, u.username, u.nickname " +
            "ORDER BY badge_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectBadgeRanking(int limit);
}
