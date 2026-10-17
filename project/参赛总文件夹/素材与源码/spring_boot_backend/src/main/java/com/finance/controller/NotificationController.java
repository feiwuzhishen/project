package com.finance.controller;

import com.finance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Map<String, Object> getNotifications(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer limit) {
        return notificationService.getNotifications(userId, limit);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam Integer userId) {
        return notificationService.getUnreadCount(userId);
    }

    @PostMapping("/read/{id}")
    public Map<String, Object> markAsRead(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        return notificationService.markAsRead(id, userId);
    }

    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead(@RequestParam Integer userId) {
        return notificationService.markAllAsRead(userId);
    }

    @PostMapping("/create")
    public Map<String, Object> createNotification(@RequestBody Map<String, Object> params) {
        Integer userId = Integer.valueOf(params.get("userId").toString());
        String type = params.get("type").toString();
        String title = params.get("title").toString();
        String content = params.get("content").toString();
        return notificationService.createNotification(userId, type, title, content);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNotification(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        return notificationService.deleteNotification(id, userId);
    }
}
