package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.finance.entity.UserNotification;
import com.finance.mapper.UserNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {

    @Autowired
    private UserNotificationMapper notificationMapper;

    public Map<String, Object> getNotifications(Integer userId, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserNotification> notifications = notificationMapper.selectList(
                new LambdaQueryWrapper<UserNotification>()
                    .eq(UserNotification::getUserId, userId)
                    .orderByDesc(UserNotification::getCreatedAt)
                    .last(limit != null ? "LIMIT " + limit : "LIMIT 20")
            );

            List<Map<String, Object>> notificationList = new ArrayList<>();
            for (UserNotification notification : notifications) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", notification.getId());
                item.put("type", notification.getType());
                item.put("title", notification.getTitle());
                item.put("content", notification.getContent());
                item.put("isRead", notification.getIsRead());
                item.put("createdAt", notification.getCreatedAt());
                notificationList.add(item);
            }

            result.put("data", notificationList);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUnreadCount(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<UserNotification>()
                    .eq(UserNotification::getUserId, userId)
                    .eq(UserNotification::getIsRead, false)
            );

            result.put("count", count);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> markAsRead(Integer notificationId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserNotification notification = notificationMapper.selectById(notificationId);
            
            if (notification == null || !notification.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("error", "通知不存在或无权操作");
                return result;
            }

            notification.setIsRead(true);
            notificationMapper.updateById(notification);

            result.put("success", true);
            result.put("message", "已标记为已读");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> markAllAsRead(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            notificationMapper.update(null,
                new LambdaUpdateWrapper<UserNotification>()
                    .eq(UserNotification::getUserId, userId)
                    .eq(UserNotification::getIsRead, false)
                    .set(UserNotification::getIsRead, true)
            );

            result.put("success", true);
            result.put("message", "全部已标记为已读");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> createNotification(Integer userId, String type, String title, String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(notification);

            result.put("success", true);
            result.put("notificationId", notification.getId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> deleteNotification(Integer notificationId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserNotification notification = notificationMapper.selectById(notificationId);
            
            if (notification == null || !notification.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("error", "通知不存在或无权操作");
                return result;
            }

            notificationMapper.deleteById(notificationId);

            result.put("success", true);
            result.put("message", "通知已删除");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
