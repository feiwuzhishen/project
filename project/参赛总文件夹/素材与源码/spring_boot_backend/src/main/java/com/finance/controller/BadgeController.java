package com.finance.controller;

import com.finance.service.BadgeService;
import com.finance.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/badge")
@CrossOrigin(origins = "*")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getAllBadges() {
        badgeService.initBadges();
        List<Map<String, Object>> badges = badgeService.getUserBadgesWithStatus(null);
        return Result.success(badges);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserBadges(@PathVariable Integer userId) {
        badgeService.initBadges();
        List<Map<String, Object>> badges = badgeService.getUserBadgesWithStatus(userId);
        return Result.success(badges);
    }

    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getBadgeRanking(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> ranking = badgeService.getBadgeRanking(limit);
        return Result.success(ranking);
    }

    @PostMapping("/check/{userId}")
    public Result<Boolean> checkAndAwardBadge(
            @PathVariable Integer userId,
            @RequestParam String conditionType,
            @RequestParam int currentValue) {
        boolean awarded = badgeService.checkAndAwardBadge(userId, conditionType, currentValue);
        return Result.success(awarded);
    }
}
