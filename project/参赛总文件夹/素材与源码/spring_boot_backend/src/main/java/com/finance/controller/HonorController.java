package com.finance.controller;

import com.finance.service.HonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/honor")
public class HonorController {

    @Autowired
    private HonorService honorService;

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserHonor(@PathVariable Integer userId) {
        return honorService.getUserHonor(userId);
    }

    @PostMapping("/add")
    public Map<String, Object> addHonor(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        String honorType = (String) params.get("honorType");
        return honorService.addHonor(userId, honorType);
    }

    @PostMapping("/post/{userId}")
    public Map<String, Object> addHonorForPost(@PathVariable Integer userId) {
        return honorService.addHonorForPost(userId);
    }

    @PostMapping("/comment/{userId}")
    public Map<String, Object> addHonorForComment(@PathVariable Integer userId) {
        return honorService.addHonorForComment(userId);
    }

    @PostMapping("/liked/{userId}")
    public Map<String, Object> addHonorForLiked(@PathVariable Integer userId) {
        return honorService.addHonorForLiked(userId);
    }

    @PostMapping("/login/{userId}")
    public Map<String, Object> addHonorForConsecutiveLogin(@PathVariable Integer userId) {
        return honorService.addHonorForConsecutiveLogin(userId);
    }

    @PostMapping("/init/{userId}")
    public Map<String, Object> initUserHonor(@PathVariable Integer userId) {
        return honorService.initUserHonor(userId);
    }
}
