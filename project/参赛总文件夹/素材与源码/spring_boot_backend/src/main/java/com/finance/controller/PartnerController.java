package com.finance.controller;

import com.finance.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/partner")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @GetMapping("/match")
    public Map<String, Object> getMatchedPartners(@RequestParam Long userId) {
        return partnerService.getMatchedPartners(userId);
    }

    @PostMapping("/request")
    public Map<String, Object> sendPartnerRequest(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        Long partnerId = params.get("partnerId");
        return partnerService.sendPartnerRequest(userId, partnerId);
    }

    @PostMapping("/accept/{id}")
    public Map<String, Object> acceptPartnerRequest(@PathVariable Long id) {
        return partnerService.acceptPartnerRequest(id);
    }

    @DeleteMapping("/reject/{id}")
    public Map<String, Object> rejectPartnerRequest(@PathVariable Long id) {
        return partnerService.rejectPartnerRequest(id);
    }

    @GetMapping("/list")
    public Map<String, Object> getPartnerList(@RequestParam Long userId) {
        return partnerService.getPartnerList(userId);
    }

    @GetMapping("/pending")
    public Map<String, Object> getPendingRequests(@RequestParam Long userId) {
        return partnerService.getPendingRequests(userId);
    }

    @PostMapping("/message")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> params) {
        Long senderId = Long.valueOf(params.get("senderId").toString());
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String content = params.get("content").toString();
        return partnerService.sendMessage(senderId, receiverId, content);
    }

    @GetMapping("/messages")
    public Map<String, Object> getMessages(@RequestParam Long userId, @RequestParam Long partnerId) {
        return partnerService.getMessages(userId, partnerId);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam Long userId) {
        return partnerService.getUnreadCount(userId);
    }
}
