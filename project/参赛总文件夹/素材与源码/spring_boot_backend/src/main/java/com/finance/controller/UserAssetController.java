package com.finance.controller;

import com.finance.service.UserAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserAssetController {

    @Autowired
    private UserAssetService userAssetService;

    @GetMapping("/list")
    public Map<String, Object> getAllUsers() {
        return userAssetService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Integer userId) {
        return userAssetService.getUserInfo(userId);
    }

    @GetMapping("/{userId}/detail")
    public Map<String, Object> getUserDetail(@PathVariable Integer userId) {
        return userAssetService.getUserDetail(userId);
    }

    @GetMapping("/{userId}/asset")
    public Map<String, Object> getUserAsset(@PathVariable Integer userId) {
        return userAssetService.getUserAssetConfig(userId);
    }

    @GetMapping("/{userId}/stocks")
    public Map<String, Object> getUserStockHolds(@PathVariable Integer userId) {
        return userAssetService.getUserStockHolds(userId);
    }

    @PutMapping("/{userId}")
    public Map<String, Object> updateUserInfo(
            @PathVariable Integer userId,
            @RequestBody Map<String, Object> params) {
        return userAssetService.updateUserInfo(userId, params);
    }
}
