package com.finance.controller;

import com.finance.service.CircleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/circle")
public class CircleController {

    @Autowired
    private CircleService circleService;

    @PostMapping
    public Map<String, Object> createCircle(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name");
        String description = (String) params.get("description");
        Integer creatorId = params.get("creatorId") != null ? (Integer) params.get("creatorId") : 1;
        Boolean isPublic = params.get("isPublic") != null ? (Boolean) params.get("isPublic") : true;
        return circleService.createCircle(name, description, creatorId, isPublic);
    }

    @GetMapping("/list")
    public Map<String, Object> getCircleList(@RequestParam(required = false) String keyword) {
        return circleService.getCircleList(keyword);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getCircleDetail(@PathVariable Integer id,
                                               @RequestParam(required = false) Integer userId) {
        return circleService.getCircleDetail(id, userId);
    }

    @PostMapping("/{id}/join")
    public Map<String, Object> joinCircle(@PathVariable Integer id,
                                          @RequestBody Map<String, Object> params) {
        Integer userId = params.get("userId") != null ? (Integer) params.get("userId") : 1;
        return circleService.joinCircle(id, userId);
    }

    @DeleteMapping("/{id}/leave")
    public Map<String, Object> leaveCircle(@PathVariable Integer id,
                                           @RequestParam Integer userId) {
        return circleService.leaveCircle(id, userId);
    }

    @PostMapping("/{id}/post")
    public Map<String, Object> createPost(@PathVariable Integer id,
                                          @RequestBody Map<String, Object> params) {
        Integer userId = params.get("userId") != null ? (Integer) params.get("userId") : 1;
        String content = (String) params.get("content");
        return circleService.createPost(id, userId, content);
    }

    @GetMapping("/{id}/posts")
    public Map<String, Object> getCirclePosts(@PathVariable Integer id) {
        return circleService.getCirclePosts(id);
    }
}
