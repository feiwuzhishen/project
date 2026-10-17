package com.finance.controller;

import com.finance.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @PostMapping("/post")
    public Map<String, Object> createPost(@RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : 1L;
        String username = params.get("username") != null ? params.get("username").toString() : "匿名用户";
        String title = params.get("title") != null ? params.get("title").toString() : "";
        String content = params.get("content") != null ? params.get("content").toString() : "";
        String tag = params.get("tag") != null ? params.get("tag").toString() : "投资心得";
        return communityService.createPost(userId, username, title, content, tag);
    }

    @GetMapping("/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId) {
        return communityService.getPosts(page, size, userId);
    }

    @GetMapping("/posts/latest")
    public Map<String, Object> getLatestPosts(
            @RequestParam(required = false) Long since,
            @RequestParam(required = false) Long userId) {
        return communityService.getLatestPosts(since, userId);
    }

    @PostMapping("/post/{id}/comment")
    public Map<String, Object> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : 1L;
        String username = params.get("username") != null ? params.get("username").toString() : "匿名用户";
        String content = params.get("content") != null ? params.get("content").toString() : "";
        return communityService.addComment(id, userId, username, content);
    }

    @GetMapping("/post/{id}/comments")
    public Map<String, Object> getComments(@PathVariable Long id) {
        return communityService.getComments(id);
    }

    @PostMapping("/post/{id}/like")
    public Map<String, Object> likePost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : 1L;
        return communityService.toggleLike(id, userId);
    }

    @DeleteMapping("/post/{id}/like")
    public Map<String, Object> unlikePost(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return communityService.toggleLike(id, userId);
    }
}
