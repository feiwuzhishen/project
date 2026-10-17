package com.finance.controller;

import com.finance.service.ExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exp")
public class ExperienceController {

    @Autowired
    private ExperienceService experienceService;

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserExperience(@PathVariable Integer userId) {
        return experienceService.getUserExperience(userId);
    }

    @PostMapping("/add")
    public Map<String, Object> addExperience(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        Integer expValue = (Integer) params.get("expValue");
        String reason = (String) params.getOrDefault("reason", "系统奖励");
        return experienceService.addExperience(userId, expValue, reason);
    }

    @PostMapping("/login/{userId}")
    public Map<String, Object> addLoginExp(@PathVariable Integer userId) {
        return experienceService.addExperience(userId, ExperienceService.EXP_DAILY_LOGIN, "每日登录");
    }

    @PostMapping("/learn/{userId}")
    public Map<String, Object> addLearnExp(@PathVariable Integer userId) {
        return experienceService.addExperience(userId, ExperienceService.EXP_LEARN_COURSE, "学习课程");
    }

    @PostMapping("/quiz/{userId}")
    public Map<String, Object> addQuizExp(@PathVariable Integer userId) {
        return experienceService.addExperience(userId, ExperienceService.EXP_COMPLETE_QUIZ, "完成测评");
    }

    @PostMapping("/post/{userId}")
    public Map<String, Object> addPostExp(@PathVariable Integer userId) {
        return experienceService.addExperience(userId, ExperienceService.EXP_POST_PUBLISH, "发布帖子");
    }

    @PostMapping("/comment/{userId}")
    public Map<String, Object> addCommentExp(@PathVariable Integer userId) {
        return experienceService.addExperience(userId, ExperienceService.EXP_COMMENT, "发表评论");
    }
}
