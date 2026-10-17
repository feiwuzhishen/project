package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.UserExperience;
import com.finance.mapper.UserExperienceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExperienceService {

    @Autowired
    private UserExperienceMapper userExperienceMapper;

    private static final int EXP_PER_LEVEL = 100;

    public static final int EXP_DAILY_LOGIN = 10;
    public static final int EXP_LEARN_COURSE = 20;
    public static final int EXP_COMPLETE_QUIZ = 30;
    public static final int EXP_POST_PUBLISH = 5;
    public static final int EXP_COMMENT = 2;

    public Map<String, Object> getUserExperience(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserExperience exp = userExperienceMapper.selectOne(
                new LambdaQueryWrapper<UserExperience>().eq(UserExperience::getUserId, userId)
            );
            if (exp == null) {
                exp = createNewExperience(userId);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", exp.getId());
            data.put("userId", exp.getUserId());
            data.put("expValue", exp.getExpValue());
            data.put("level", exp.getLevel());
            data.put("createdAt", exp.getCreatedAt());
            data.put("updatedAt", exp.getUpdatedAt());
            data.put("nextLevelExp", (exp.getLevel()) * EXP_PER_LEVEL);
            data.put("currentLevelExp", (exp.getLevel() - 1) * EXP_PER_LEVEL);
            data.put("progress", calculateProgress(exp.getExpValue(), exp.getLevel()));
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> addExperience(Integer userId, Integer expToAdd, String reason) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserExperience exp = userExperienceMapper.selectOne(
                new LambdaQueryWrapper<UserExperience>().eq(UserExperience::getUserId, userId)
            );
            if (exp == null) {
                exp = createNewExperience(userId);
            }
            int oldLevel = exp.getLevel();
            int newExpValue = exp.getExpValue() + expToAdd;
            int newLevel = calculateLevel(newExpValue);
            exp.setExpValue(newExpValue);
            exp.setLevel(newLevel);
            exp.setUpdatedAt(LocalDateTime.now());
            userExperienceMapper.updateById(exp);
            boolean levelUp = newLevel > oldLevel;
            Map<String, Object> data = new HashMap<>();
            data.put("expValue", newExpValue);
            data.put("level", newLevel);
            data.put("expAdded", expToAdd);
            data.put("reason", reason);
            data.put("levelUp", levelUp);
            data.put("oldLevel", oldLevel);
            data.put("progress", calculateProgress(newExpValue, newLevel));
            result.put("data", data);
            result.put("success", true);
            result.put("message", levelUp ? "恭喜升级！当前等级：Lv" + newLevel : "获得" + expToAdd + "经验值");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private UserExperience createNewExperience(Integer userId) {
        UserExperience exp = new UserExperience();
        exp.setUserId(userId);
        exp.setExpValue(0);
        exp.setLevel(1);
        LocalDateTime now = LocalDateTime.now();
        exp.setCreatedAt(now);
        exp.setUpdatedAt(now);
        userExperienceMapper.insert(exp);
        return exp;
    }

    private int calculateLevel(int expValue) {
        return (expValue / EXP_PER_LEVEL) + 1;
    }

    private int calculateProgress(int expValue, int level) {
        int currentLevelBaseExp = (level - 1) * EXP_PER_LEVEL;
        int nextLevelBaseExp = level * EXP_PER_LEVEL;
        int progressInCurrentLevel = expValue - currentLevelBaseExp;
        int expNeededForNextLevel = nextLevelBaseExp - currentLevelBaseExp;
        return (int) ((double) progressInCurrentLevel / expNeededForNextLevel * 100);
    }
}
