package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.Badge;
import com.finance.entity.UserBadge;
import com.finance.mapper.BadgeMapper;
import com.finance.mapper.UserBadgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BadgeService {

    @Autowired
    private BadgeMapper badgeMapper;

    @Autowired
    private UserBadgeMapper userBadgeMapper;

    public List<Badge> getAllBadges() {
        return badgeMapper.selectList(null);
    }

    public List<Map<String, Object>> getUserBadges(Integer userId) {
        return userBadgeMapper.selectUserBadgesWithInfo(userId);
    }

    public List<Map<String, Object>> getBadgeRanking(int limit) {
        return userBadgeMapper.selectBadgeRanking(limit);
    }

    public List<Map<String, Object>> getUserBadgesWithStatus(Integer userId) {
        List<Badge> allBadges = getAllBadges();
        List<Map<String, Object>> userBadges = getUserBadges(userId);
        Set<Integer> earnedBadgeIds = new HashSet<>();
        
        for (Map<String, Object> ub : userBadges) {
            earnedBadgeIds.add((Integer) ub.get("badge_id"));
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Badge badge : allBadges) {
            Map<String, Object> badgeInfo = new HashMap<>();
            badgeInfo.put("id", badge.getId());
            badgeInfo.put("name", badge.getName());
            badgeInfo.put("description", badge.getDescription());
            badgeInfo.put("iconUrl", badge.getIconUrl());
            badgeInfo.put("conditionType", badge.getConditionType());
            badgeInfo.put("conditionValue", badge.getConditionValue());
            badgeInfo.put("earned", earnedBadgeIds.contains(badge.getId()));
            
            for (Map<String, Object> ub : userBadges) {
                if (ub.get("badge_id").equals(badge.getId())) {
                    badgeInfo.put("earnedAt", ub.get("earned_at"));
                    break;
                }
            }
            result.add(badgeInfo);
        }
        return result;
    }

    public boolean checkAndAwardBadge(Integer userId, String conditionType, int currentValue) {
        LambdaQueryWrapper<Badge> badgeQuery = new LambdaQueryWrapper<>();
        badgeQuery.eq(Badge::getConditionType, conditionType);
        List<Badge> badges = badgeMapper.selectList(badgeQuery);
        
        boolean awarded = false;
        for (Badge badge : badges) {
            if (currentValue >= badge.getConditionValue()) {
                LambdaQueryWrapper<UserBadge> ubQuery = new LambdaQueryWrapper<>();
                ubQuery.eq(UserBadge::getUserId, userId)
                       .eq(UserBadge::getBadgeId, badge.getId());
                
                if (userBadgeMapper.selectCount(ubQuery) == 0) {
                    UserBadge userBadge = new UserBadge();
                    userBadge.setUserId(userId);
                    userBadge.setBadgeId(badge.getId());
                    userBadge.setEarnedAt(LocalDateTime.now());
                    userBadgeMapper.insert(userBadge);
                    awarded = true;
                }
            }
        }
        return awarded;
    }

    public void initBadges() {
        if (badgeMapper.selectCount(null) > 0) {
            return;
        }
        
        List<Badge> defaultBadges = Arrays.asList(
            createBadge("学习先锋", "连续学习7天", "📚", "STUDY_DAYS", 7),
            createBadge("投资达人", "完成10次模拟交易", "💰", "TRADE_COUNT", 10),
            createBadge("社区之星", "发布10篇帖子", "⭐", "POST_COUNT", 10),
            createBadge("连续登录", "连续登录30天", "🔥", "LOGIN_DAYS", 30)
        );
        
        for (Badge badge : defaultBadges) {
            badgeMapper.insert(badge);
        }
    }

    private Badge createBadge(String name, String description, String iconUrl, String conditionType, int conditionValue) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(description);
        badge.setIconUrl(iconUrl);
        badge.setConditionType(conditionType);
        badge.setConditionValue(conditionValue);
        return badge;
    }
}
