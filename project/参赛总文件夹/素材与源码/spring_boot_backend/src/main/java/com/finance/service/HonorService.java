package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.UserHonor;
import com.finance.mapper.UserHonorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class HonorService {

    @Autowired
    private UserHonorMapper userHonorMapper;

    private static final int HONOR_FOR_POST = 5;
    private static final int HONOR_FOR_COMMENT = 2;
    private static final int HONOR_FOR_LIKED = 3;
    private static final int HONOR_FOR_CONSECUTIVE_LOGIN = 50;

    public Map<String, Object> getUserHonor(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserHonor honor = userHonorMapper.selectOne(
                new LambdaQueryWrapper<UserHonor>().eq(UserHonor::getUserId, userId)
            );
            if (honor != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", honor.getId());
                data.put("userId", honor.getUserId());
                data.put("honorValue", honor.getHonorValue());
                data.put("honorLevel", honor.getHonorLevel());
                data.put("createdAt", honor.getCreatedAt());
                data.put("updatedAt", honor.getUpdatedAt());
                result.put("data", data);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "用户功勋值记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> addHonor(Integer userId, String honorType) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserHonor honor = userHonorMapper.selectOne(
                new LambdaQueryWrapper<UserHonor>().eq(UserHonor::getUserId, userId)
            );
            
            if (honor == null) {
                honor = new UserHonor();
                honor.setUserId(userId);
                honor.setHonorValue(0);
                honor.setHonorLevel("新手");
                honor.setCreatedAt(LocalDateTime.now());
                honor.setUpdatedAt(LocalDateTime.now());
                userHonorMapper.insert(honor);
            }

            int addValue = getHonorValueByType(honorType);
            int newValue = honor.getHonorValue() + addValue;
            String newLevel = calculateHonorLevel(newValue);

            honor.setHonorValue(newValue);
            honor.setHonorLevel(newLevel);
            honor.setUpdatedAt(LocalDateTime.now());
            userHonorMapper.updateById(honor);

            Map<String, Object> data = new HashMap<>();
            data.put("honorValue", newValue);
            data.put("honorLevel", newLevel);
            data.put("addedValue", addValue);
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> addHonorForPost(Integer userId) {
        return addHonor(userId, "post");
    }

    public Map<String, Object> addHonorForComment(Integer userId) {
        return addHonor(userId, "comment");
    }

    public Map<String, Object> addHonorForLiked(Integer userId) {
        return addHonor(userId, "liked");
    }

    public Map<String, Object> addHonorForConsecutiveLogin(Integer userId) {
        return addHonor(userId, "consecutive_login");
    }

    private int getHonorValueByType(String honorType) {
        switch (honorType) {
            case "post":
                return HONOR_FOR_POST;
            case "comment":
                return HONOR_FOR_COMMENT;
            case "liked":
                return HONOR_FOR_LIKED;
            case "consecutive_login":
                return HONOR_FOR_CONSECUTIVE_LOGIN;
            default:
                return 0;
        }
    }

    private String calculateHonorLevel(int honorValue) {
        if (honorValue <= 100) {
            return "新手";
        } else if (honorValue <= 500) {
            return "达人";
        } else if (honorValue <= 1000) {
            return "专家";
        } else {
            return "大师";
        }
    }

    public Map<String, Object> initUserHonor(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserHonor existingHonor = userHonorMapper.selectOne(
                new LambdaQueryWrapper<UserHonor>().eq(UserHonor::getUserId, userId)
            );
            
            if (existingHonor != null) {
                result.put("success", false);
                result.put("error", "用户功勋值已存在");
                return result;
            }

            UserHonor honor = new UserHonor();
            honor.setUserId(userId);
            honor.setHonorValue(0);
            honor.setHonorLevel("新手");
            honor.setCreatedAt(LocalDateTime.now());
            honor.setUpdatedAt(LocalDateTime.now());
            userHonorMapper.insert(honor);

            result.put("success", true);
            result.put("data", Map.of(
                "userId", userId,
                "honorValue", 0,
                "honorLevel", "新手"
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
